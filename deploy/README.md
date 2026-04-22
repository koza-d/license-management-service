# deploy/ — AWS $100 크레딧용 배포 번들

2~3개월 단기 AWS 배포에 필요한 모든 구성(Terraform / Docker / Caddy / GitHub Actions / 백업)이 여기에 모여 있습니다. 크레딧 소진 후 Oracle Always Free 같은 다른 VM 환경으로 옮겨도 `docker-compose.yml` + `.env` + `Caddyfile` 세 개만 가져가면 동일하게 동작하도록 구성했습니다.

## 디렉터리 구조

```
deploy/
├── Dockerfile               # 멀티스테이지, ARM64 Graviton 빌드
├── docker-compose.yml       # caddy + app + redis
├── Caddyfile                # TLS 자동, 보안 헤더
├── .env.example             # EC2 에 /opt/lms/.env 로 복사
├── scripts/
│   └── backup.sh            # mysqldump → gpg 암호화 → S3 업로드
└── terraform/
    ├── versions.tf
    ├── variables.tf
    ├── main.tf              # VPC / EC2 / RDS / S3 / IAM / Budgets
    ├── outputs.tf
    ├── user_data.sh         # Docker 설치 + 스왑 + 타임존
    └── terraform.tfvars.example
```

## 사전 준비

- AWS 계정 (Access Key + Secret), `aws configure` 완료
- Terraform 1.6+
- 도메인 1개 + Cloudflare 계정 (무료)
- GitHub 리포지토리 (GHCR 권한)

## 1. 인프라 생성 (Terraform, 약 15분)

```bash
cd deploy/terraform
cp terraform.tfvars.example terraform.tfvars
# terraform.tfvars 값 채우기 (SSH 공개키, 내 IP, DB 비번, 예산 알람 이메일)

terraform init
terraform plan
terraform apply
```

완료 후 출력되는 값을 기록:

- `ec2_public_ip` → Cloudflare DNS A 레코드
- `rds_endpoint` → `.env` 의 `DB_HOST`
- `backup_bucket` → `.env` 의 `BACKUP_BUCKET`

## 2. Cloudflare 설정

- `api.example.com` A 레코드 → `ec2_public_ip`, **Proxy 상태 ON**(주황 구름)
- SSL/TLS 모드: **Full (strict)**
- Security → WAF Rate Limiting Rules (최소 3개):
  - `/api/auth/*` : 30/min per IP
  - `/api/verification/*` : 120/min per IP
  - `/api/admin/*` : 60/min per IP

## 3. EC2 초기 셋업 (최초 1회)

```bash
# Terraform 이 출력한 public IP 로 SSH
ssh -i ~/.ssh/lms_ed25519 ubuntu@<ec2_public_ip>

# 앱 디렉터리 이동 (user_data 가 이미 생성)
cd /opt/lms

# .env 업로드 (로컬에서)
#   scp -i ~/.ssh/lms_ed25519 deploy/.env.example ubuntu@<ip>:/opt/lms/.env
#   값 채운 후
chmod 600 /opt/lms/.env
```

### 스키마 적용 (ddl-auto: none 이므로 수동)

```bash
# 리포 clone 또는 scp 로 docs/schema/*.sql 가져오기
mysql -h "$DB_HOST" -u "$DB_USERNAME" -p"$DB_PASSWORD" "$DB_NAME" \
  < docs/schema/admin_audit_log.sql

# Flyway 도입 전까지는 각 도메인 테이블 DDL 을 순서대로 apply.
# 장기적으론 src/main/resources/db/migration/V*.sql 로 옮겨
# ./gradlew flywayMigrate 로 자동화 권장.
```

## 4. GitHub Actions Secrets 등록

리포 Settings → Secrets and variables → Actions → New repository secret

| Secret | 값 |
|---|---|
| `EC2_HOST` | Terraform `ec2_public_ip` |
| `EC2_SSH_KEY` | `~/.ssh/lms_ed25519` 프라이빗키 내용 전체 |
| `GHCR_USERNAME` | GitHub 아이디 |
| `GHCR_READ_TOKEN` | GHCR `read:packages` PAT (EC2 가 이미지 pull 용) |

Environment(Settings → Environments) → **prod** 생성, 필요시 수동 승인 토글.

## 5. 배포

`main` 브랜치에 push → `deploy.yml` 자동 실행:

1. `bootJar` 빌드
2. ARM64 Docker 이미지 → GHCR 푸시
3. EC2 로 compose/Caddyfile scp
4. `docker compose pull & up -d`
5. `/` 스모크 테스트

수동 실행: Actions → Deploy → Run workflow.

## 6. 백업 cron 등록 (최초 1회)

```bash
ssh ubuntu@<ec2_public_ip>
crontab -e
# 아래 한 줄 추가 (매일 02:00 KST)
0 2 * * * /opt/lms/scripts/backup.sh >> /var/log/lms-backup.log 2>&1
```

복구 검증(월 1회 권장):

```bash
aws s3 cp s3://$BACKUP_BUCKET/lms-YYYYMMDD-HHMMSS.sql.gz.gpg - \
  | gpg --batch --passphrase "$BACKUP_GPG_PASSPHRASE" --decrypt \
  | gunzip | head -50
```

## 7. 운영 중 체크리스트

매주 확인:
- [ ] AWS Billing 콘솔 → 크레딧 잔액
- [ ] Budgets 알람 이메일 도달 여부
- [ ] RDS Performance Insights → Top SQL
- [ ] `docker compose logs --tail=200 app` 에러 패턴
- [ ] `df -h` 디스크 여유 (EBS 30GB, Docker 이미지 정리)

위험한 버튼 (실수로 누르면 크레딧 즉사):
- ❌ RDS Multi-AZ 활성화
- ❌ ElastiCache 생성
- ❌ NAT Gateway
- ❌ Route 53 호스팅존 신규 생성 (Cloudflare DNS 쓰는 중)
- ❌ Storage autoscaling 활성화
- ❌ CloudWatch Logs retention 연장

## 8. 종료 / 이전 (크레딧 $60 도달 시 시작)

1. Oracle Always Free VM 또는 Hetzner 신규 VM 준비
2. 신규 VM 에서 `docker compose up -d`(MySQL/Redis 포함, RDS 대신) 구성
3. 최종 `mysqldump` 떠서 신규 VM MySQL 로 restore
4. Cloudflare DNS A 레코드를 신규 VM IP 로 변경 (TTL 60초로 미리 낮춰두기)
5. 검증 후 AWS 리소스 정리:
   ```bash
   cd deploy/terraform
   # RDS deletion_protection = true 이므로 먼저 false 로 수정 후 apply
   terraform destroy
   ```
6. S3 백업 버킷은 수동 삭제(force_destroy=false 기본값)
7. 콘솔에서 잔여 리소스 확인: EIP, 스냅샷, AMI, CloudWatch 로그 그룹

## 문제 해결

### Redis keyspace 이벤트가 안 뜸
```bash
docker compose exec redis redis-cli CONFIG GET notify-keyspace-events
# 비어 있으면:
docker compose exec redis redis-cli CONFIG SET notify-keyspace-events Ex
```
compose 재생성: `docker compose up -d --force-recreate redis`

### Caddy TLS 발급 실패
- Cloudflare Proxy 가 ON 이면 80/443 이 Cloudflare 를 거침.
- SSL/TLS 모드를 일시적으로 "Full"(strict 아님) 로 낮추고 발급 후 strict 복귀.
- 또는 Cloudflare "Origin Certificate" 발급받아 Caddy 에 직접 탑재.

### Spring Boot OOM
`docker-compose.yml` 에 `environment.JAVA_OPTS=-Xmx768m -Xms384m ...` 로 override.
