# Licensify

소프트웨어 라이센스 발급 · 관리 · 검증 SaaS의 백엔드(REST API) 레포지토리다.

**https://licensify.kr** 에서 실제로 가입하고 사용할 수 있다.

<br>

## 목차

- 프로젝트 개요
- 프로젝트 배경
- 프로젝트 소개
- 기술 스택
- 주요 기능
- 인프라 구성도
- ERD
- SDK API 플로우 차트
- 트러블 슈팅

<br>

## 1. 프로젝트 개요

| 항목 | 내용 |
|---|---|
| 한 줄 소개 | 프로그램 판매자를 위한 라이센스 인증 서버를 SaaS로 제공한다 |
| 서비스 | https://licensify.kr (Vercel 배포, AI 제작) |
| API 서버 | `api.licensify.kr` (EC2 + Docker Compose, blue-green 배포) |
| 인원 | 개인 프로젝트 (일부 기간 1명 참여) |
| 이 레포 | 백엔드 REST API |
| 함께 만든 것 | 프론트(Next.js 별도 레포), C# 인증 SDK(공통 규약을 기반으로 AI가 제작) |

프로그램을 판매하는 개발자가 라이센스 키를 발급하고, 실행 중인 프로그램이 그 키를 검증하고, 동시 사용을 막고,
버전과 무결성을 확인하는 일련의 과정을 Licensify 서버가 대신 처리한다.
개발자는 웹 콘솔에서 라이센스를 관리하고, 자신의 프로그램에는 SDK를 붙여 함수 한 번으로 인증을 붙인다.

<br>

## 2. 프로젝트 배경

프로그램을 만들어서 구독 형식으로 판매하거나, 배포하고 싶을 때 아래 문제가 따라온다.
- 라이센스 키 발급
- 라이센스 기간 설정
- 한 개 키당 동시 사용 제한
- 버전 변경 시 버전관리
- 해시 무결성 검증
- 사용 통계

이 기능들을 클라이언트 내부에서만 해결할 수는 없다. 프로그램을 만드는 사람은 본인 프로그램과 본질과는 무관한 인증 서버를 따로 구축 및 운영해야하고, 거기에 따른 암호화 프로토콜이나 라이센스 관리 시스템을 직접 제작해야한다.

**Licensify는 그 인증+관리 서버를 대신 제공한다.**

<br>

## 3. 프로젝트 소개

Licensify는 크게 아래 두 가지를 제공한다.

**① 웹 콘솔**
- 소프트웨어 등록, 관리
- 라이센스 발급 · 정지 · 만료
- 버전 · 파일 해시 등록
- 서버에 저장되는 변수 설정
- 활성 세션 관리 및 조회
- 사용 통계

**② C# SDK** - 프로그램에 넣으면 인증 · 암호화 · 하트비트 · 재시도 및 복잡한 로직은 SDK 내부에서 처리한다.
앱 개발자에게 남는 것은 아래가 전부다.

```csharp
var client = new LicenseClient(appId, clientVersion);

// init(버전·무결성) → verify(인증·키교환) → 하트비트 자동 시작까지 한 번에
var session = await client.Authenticate(licenseKey);

var remaining = session.RemainingMillis;          // 라이센스 만료까지 남은 시간
var theme = session.LocalVariables["theme"];      // 서버에서 내려준 사용자별 설정
await session.SetLocalVariable("theme", "dark");  // 변경은 서버에 반영

await client.Release();                           // 앱 종료 시
```

하지만 어디까지나 SDK와 서버가 하는 역할은 **제 3자 공격**이나, **가짜서버**를 막는 조치이지 클라이언트 프로그램 자체의 바이너리를 수정해 인증 분기를 제거하는 건 절대 막을 수 없다.

바이너리를 수정하는건 **난독화를 통해 어렵게 하는 수 밖에** 없고, **그 몫은 개발자 본인**에게 있다.

그리고 SDK는 하드웨어 정보를 수집하지 않는다. 인증 자체는 서버 연결을 통해서만 가능하고 **라이센스 키로만 인증**을 한다.
<br>

## 4. 기술 스택

| 구분 | 기술 |
|---|---|
| Language / Framework | Java 17, Spring Boot 3.4.2 |
| Data | MySQL 8, Redis 7, Spring Data JPA, QueryDSL 5 |
| Auth | Spring Security, JWT, OAuth2 소셜 로그인(Google · GitHub · Naver) |
| Crypto (SDK) | Ed25519, X25519, HKDF-SHA256, AES-256-GCM |
| Payment | Toss Payments (빌링키 정기 결제) |
| Infra | Docker Compose, Caddy 2, GitHub Actions, AWS EC2, AWS S3 |
| Test | k6 (부하 테스트), Postman, QA 테스트 |

<br>

## 5. 주요 기능

<details>
<summary><b>라이센스 라이프사이클</b> - 발급부터 활성화 · 만료 · 정지까지</summary>
발급 시점에는 INACTIVE 상태로 만들어지고, 첫 인증이 들어온 순간 활성화되면서 만료 시각이 계산된다.

- 플랜별로 동시에 활성화할 수 있는 라이센스 수(좌석)에 한도를 건다.
- 그와 별개로 발급 자체에도 상한을 둬서, 무료 플랜으로 키를 무한정 찍어내는 것을 막는다.
- 정지 · 만료 상태는 다음 인증부터 즉시 거부되고, 이미 붙어 있는 세션도 함께 끊는다.

</details>

<br>
<details>
<summary><b>라이센스당 단일 활성 세션</b> - 한 키로 두 대에서 동시에 못 쓴다</summary>
인증에 성공하면 Redis에 세션이 생기고, 라이센스 하나당 세션은 항상 한 개만 존재한다.
다른 기기에서 같은 키로 인증하면 기존 세션을 끊고 새 세션으로 교체한다.

- 세션 TTL은 60초이고, SDK가 20초 주기로 보내는 하트비트가 이를 연장한다.
  프로그램이 강제 종료돼도 60초 뒤에는 자동으로 자리가 반납된다.
- 만료는 Redis 키 TTL에 맡기고, 만료 이벤트를 받아 관련 상태를 정리한다.
- 이벤트를 놓쳐 남은 유령 세션은 스케줄러가 회수한다.

</details>

<br>
<details>
<summary><b>버전 · 무결성 관리</b> - 구버전 차단과 바이너리 변조 판정</summary>
소프트웨어의 버전과 실행 파일 해시를 콘솔에 등록해두면, SDK가 실행 시점에 서버로 물어본다.

- 버전을 여러개 등록해두고 각 버전을 사용가능 / 불가능, 해시, 다운로드 링크를 설정 가능하다.
- 프로그램이 사용 불가능한 버전인 경우 인증에 실패하며 업데이트 주소를 내려준다.
- 프로그램이 사용 가능한 버전이고, 서버에 설정해둔 버전과 다르면 인증에 성공하며 업데이트 주소를 내려준다. (업데이트 유도, 개발자 몫)
- 등록된 해시와 다르면 변조로 판정한다.
- 이 응답은 서버가 Ed25519로 서명해서 보낸다. 가짜 서버가 "너는 최신 버전이야" 라고 답하는 것을 막기 위해서다.
</details>

<br>
<details>
<summary><b>서버 변수</b> - 재배포 없이 프로그램 동작을 바꾼다</summary>
→ 소프트웨어 전체에 적용되는 전역 변수와, 라이센스별로 다른 값을 갖는 로컬 변수를 둔다.
기능 플래그를 켜고 끄거나 사용자마다 다른 설정을 주는 용도다.

- 전역 변수는 콘솔에서 바꾸면 다음 인증부터 모든 사용자에게 적용된다.
- 로컬 변수는 SDK에서 읽고 쓸 수 있어, 프로그램이 서버에 상태를 남기는 통로가 된다.

</details>

<br>
<details>
<summary><b>구독 · 결제</b> - 플랜별 한도와 정기 결제</summary>
→ FREE / PRO / ENTERPRISE 플랜에 따라 활성 라이센스 한도가 달라진다.

- 카드 정보는 저장하지 않고 Toss가 발급한 빌링키를 암호화해서 보관해 정기 결제를 처리한다.
- 결제에 실패해도 즉시 끊지 않고 `PAST_DUE`로 유예를 둔 뒤, 재시도까지 실패하면 그때 다운그레이드한다.

</details>

<br>
<details>
<summary><b>계정 · 인증</b> - JWT와 소셜 로그인</summary>
→ JWT 기반 인증에 Google · GitHub · Naver 소셜 로그인을 지원한다.

인가 코드 → 토큰 → 사용자 정보로 이어지는 흐름을 WebClient로 구현했다.
provider를 인터페이스로 추상화해서 소셜 하나를 추가하거나 빼는 데 용이하다.

</details>

<br>
<details>
<summary><b>운영 · 감사</b> - 관리자 행위와 변경 이력 기록</summary>
→ 관리자 API와 사용자 API를 컨트롤러 · 서비스 계층에서 분리했다.<br>
→ 관리자의 조작과 라이센스 · 소프트웨어의 변경 사항은 이벤트로 발행해 비동기로 기록한다.<br>
로그 기록은 별도의 트랜잭션으로, 로그 쓰기가 실패해도 원래 작업을 막거나 되돌리지 않는다.

</details>

<br>

## 6. 인프라 구성도

<img width="1674" height="882" alt="Licensify Infra" src="https://github.com/user-attachments/assets/3283ec67-bf15-4596-9788-c048a012edd0" />

- **Caddy 2가 80/443 단일 진입점**이고 앱 컨테이너는 내부 8080만 연다.
  인증서 자동 발급 · 갱신이 기본이라 HTTPS 설정 부담이 없고, blue-green 트래픽 전환은 Caddyfile 한 줄 수정 + 리로드로 전환한다.
- **blue-green 무중단 배포(stop 전략)** - EC2 한 대에서 무중단 전환을 하되 메모리를 아끼려고, 트래픽을 받지 않는 쪽 컨테이너는 띄워두지 않고 정지시킨다. 배포는 `유휴 쪽 기동 → 헬스체크 → Caddy 대상 교체 → 공개 엔드포인트 스모크 테스트`로 진행하고, 실패하면 자동으로 되돌린다.
- **이미지 태그를 커밋 SHA로 관리**해서 지금 떠 있는 컨테이너가 어느 커밋인지 항상 추적된다.
- **백업** - cron으로 `mysqldump` 후 S3에 업로드한다. 백업이 성공할 때마다 healthchecks.io로 핑을 보내고, 정해진 시간에 핑이 오지 않으면 메일로 알림을 받는다.

<br>

## 7. ERD

<img width="918" height="1076" alt="licensify-erd" src="https://github.com/user-attachments/assets/1806079f-78ef-4965-bd71-127f612574af" />

이력 · 감사 테이블은 생략

<br>

## 8. SDK API 상세 및 플로우 차트

SDK에는 간단한 함수 하나만 남기고, 복잡한 인증 · 암호화 · 하트비트 · 재시도는 SDK가 처리한다.

**전제** - 서버만 아는 Ed25519 개인키와 SDK에 내장된 공개키가 있다.<br>
SDK에는 Ed25519 공개키를 여러 개 넣어두고 서버가 어느 것을 쓸지 `keyId`로 알려준다(키 회전 대비).

```
init → verify → [ hb 반복 · lv 수시 ] → release
```

| 단계 | 역할 | 보안 조치 |
|---|---|---|
| `/init` | 버전 · 파일 해시 검사 (무상태) | 응답에 Ed25519 서명을 붙여 가짜 서버가 SDK를 속이는 것을 차단 |
| `/verify` | 라이센스 검증 + 세션 생성 | 양측 일회용 X25519 키쌍으로 ECDH → HKDF-SHA256으로 방향별 키 2개(`keyC2S` / `keyS2C`) 도출. 응답은 AES-256-GCM 암호화 + 클라이언트 nonce를 묶어 Ed25519 서명 → 인증된 키 교환 |
| `/hb` | 세션 TTL(60s) 연장 | C2S 암호문을 `keyC2S`로 복호화해 무결성을 증명. nonce는 방향별 시퀀스에서 만들어 GCM nonce 재사용을 막는다 |
| `/release`<br>`/lv` | 세션 해제 / 로컬 변수 수정 | hb와 동일한 C2S 암호문 검증을 통과해야 동작 |

TLS가 전송 구간을 지키는데도 앱 레이어에서 한 번 더 서명 · 암호화하는 이유는,
**라이센스 인증에서는 최종 사용자가 곧 공격자**이기 때문이다.
사용자가 고의로 TLS 검증을 끄고 프록시를 세워도 가짜 서버를 걸러내야 한다.

- **Ed25519** — RSA 대비 키가 짧고 서명 · 검증이 빠르다.
- **X25519** — P-256 대비 구현 실수로 인한 취약점 여지가 적고 빠르다.
- **AES-256-GCM** — 인증 태그가 있어 응답 위조를 잡아낸다.
- **HKDF-SHA256** — 하나의 공유 비밀에서 방향별 키 2개를 안전하게 뽑는다. AES가 요구하는 균일한 난수 키를 만들어준다.

**리플레이 방지** — 서버 → 클라이언트 응답은 클라이언트가 `serverSeq`의 단조 증가를 확인해 재생을 거부한다.
클라이언트 → 서버 방향은 서버가 `clientSeq`를 세션 간 저장 · 비교하지 않는다.
이 방향의 재생은 세션 연장 이상의 효과가 없어 치명적이지 않다고 판단했고, 시퀀스의 역할은 방향별 GCM nonce의 유일성 보장이다.

<details>
<summary><b>/init 플로우 차트</b> </summary>

<br>

<img width="612" height="532" alt="init_flowchart drawio" src="https://github.com/user-attachments/assets/366a346b-b2a0-4ccf-bec7-e0cf0ee04745" />

</details>

<details>
<summary><b>/verify 플로우 차트</b></summary>

<br>

<img width="621" height="912" alt="verify_flowchart drawio" src="https://github.com/user-attachments/assets/d1357e82-e38e-4b8c-8ce1-90b273f43b05" />

</details>

<details>
<summary><b>/hb 플로우 차트</b></summary>

<br>

<img width="638" height="607" alt="hb_flowchart drawio" src="https://github.com/user-attachments/assets/d9c3cf7e-9ba2-4d32-9400-8425597f3648" />

</details>

<details>
<summary><b>/release · /lv  플로우 차트</b></summary>

<br>

| `/release` | `/lv` |
|---|---|
|<img width="612" height="504" alt="release_flowchart drawio" src="https://github.com/user-attachments/assets/76a7d5bb-8178-47af-b8b5-5afd3571d0ea" /> | <img width="612" height="504" alt="sdk_lv_flowchart drawio" src="https://github.com/user-attachments/assets/0f4c3096-98ce-411b-928f-d7ace842de1c" /> |

</details>

<br>

## 9. 트러블 슈팅

<details>
<summary><b>SDK 인증 API 데드락</b> - 락 업그레이드 경합 제거, 그 대가로 잃은 처리량을 락 범위 축소로 회복 (p95 577ms → 270ms)</summary>

→ 상세: [SDK 라이센스 인증 API 데드락 해결기](https://potential-3.tistory.com/16)


**증상**

부하 테스트에서 같은 라이센스로 인증 요청이 겹칠 때 일부 요청이 데드락으로 실패했다.<br>
세션이 이미 있는 라이센스에 2건 이상의 요청이 동시에 들어오면 재현된다.

<br>

**원인 - 공유락을 쥔 채로 서로 배타락을 원함**

라이센스 재인증은 `기존 세션 해제 → 신규 인증` 순서로 동작하고, 한 트랜잭션 안에서 두 가지 일이 일어난다.

1. `session_log`를 insert한다. 이 테이블은 `licenses`를 FK로 참조하므로, DB가 부모 행에 **공유락**을 건다.
2. 이어서 라이센스 상태를 업데이트한다. 같은 행에 **배타락**이 필요하다.

두 트랜잭션이 거의 동시에 1번을 통과하면 둘 다 같은 라이센스 행의 공유락을 쥔 상태가 된다.
이 상태에서 각자 2번으로 넘어가면, 배타락을 얻으려면 상대가 공유락을 놓아야 하는데 상대도 똑같이 기다리고 있다.
무한정 락을 놓기를 기다리고 있는 상태의 데드락이다.

<br>

**조치 1 - 조회 시점부터 배타락을 잡는다**

라이센스를 조회하는 시점에 `PESSIMISTIC_WRITE`를 걸었다.
공유락을 거쳤다가 올라가는 게 문제였으므로, 아예 처음부터 배타락으로 시작하면 승격 자체가 없어진다.
두 번째 트랜잭션은 조회 단계에서 그냥 대기하다가 순서대로 처리된다.

데드락은 사라졌지만 최대 처리량이 294 → 68 TPS로 떨어졌다.

<br>

**조치 2 - 락 범위를 라이센스 행으로 좁힌다**

원인은 조회 쿼리에 걸려 있던 fetch join이었다.
`SELECT ... FOR UPDATE`가 join된 테이블의 행까지 함께 잠그기 때문에, 라이센스만 잠그려던 것이 소프트웨어 행까지 잠그고 있었다.
같은 소프트웨어에 속한 요청 전체가 그 행 하나를 두고 줄을 서면서 모두 직렬 처리가 된 것이다.

라이센스 조회에서 software fetch join을 분리해 별도로 가져오도록 바꿨다.

| 단계 | 조치 | 최대 TPS | p95 |
|---|---|---|---|
| 1 | 락 적용 전 (요청의 0.03~0.05%에서 데드락) | 294 | 577ms |
| 2 | 라이센스 조회에 `PESSIMISTIC_WRITE` | 68 | 820ms |
| 3 | software fetch join 분리 | 281 | 270ms |


- 2단계의 감소폭과, 3단계의 회복 폭은 모든 라이센스가 소프트웨어 1건에 속한 테스트 데이터라 락 경합이 극대화된 최악 케이스 기준이다.
  소프트웨어가 여러 개로 분산된 운영 환경에서는 이만큼 벌어지지 않는다.

<br>

**배운 것** - 락은 "어디까지 잠궜는가"가 성능을 결정하고, 락을 걸어야하는 지점과 영역을 정확히 인지해야한다.
</details>

<br>
<details>
<summary><b>verify 동시 요청 시 세션 중복 생성</b> — Redis 원자 연산으로 차단</summary>

→ 상세: [issue #2 - verify 동시 요청 동시성 문제](https://github.com/koza-d/license-management-service/issues/2)


※ 데드락 해결 시 라이센스 조회에 배타락을 들고 시작해 실질적으로 여기서 문제는 발생하지 않는다.<br>
※ 데드락 문제 해결 전, 해당 문제를 직면했었기에 이 시점엔 Redis단에 경합이 있었음<br>


앞의 데드락과 같은 `verify` 동시 요청에서 나온 문제지만, 이쪽은 DB 락이 아니라 Redis 상태의 경합이다.<br>

**증상**

같은 라이센스 키로 요청이 동시에 들어오면 "라이센스당 활성 세션 1개"라는 규칙이 깨지고 세션이 두 개 만들어졌다.
한 개 키로 두 대에서 동시에 프로그램을 쓸 수 있게 되므로, 이 서비스의 치명적인 버그였다.

<br>

**원인 - 확인과 저장 사이의 틈**

세션 생성은 `활성 세션이 있는지 조회 → 없으면 저장`으로 동작했다.
두 요청이 거의 같은 순간에 도착하면 둘 다 "없음"을 보고 지나간 뒤, 둘 다 저장에 성공한다.
확인과 저장이 서로 다른 구간에 있는 명령이라 그 사이에 다른 요청이 끼어들 수 있는 문제였다.

<br>

**조치 - SETNX로 확인과 저장을 한 번에**

`lock:{licenseId}`를 `setIfAbsent`(Redis `SET NX`)로 저장하고 세션에 필요한 키를 모두 저장하고나면 `lock:{licenseId}`를 제거해 세션 저장 중
"없으면 저장한다"가 하나의 명령으로 처리되고 Redis가 이를 원자적으로 보장하므로, 같은 키로는 한 요청만 성공한다.
선점에 실패한 쪽은 `ALREADY_USE_LICENSE`로 거절된다.

동일 라이센스에 2개 스레드가 동시에 요청하는 테스트에서 성공 1건 / 실패 1건을 확인했다.

<br>

**이후 - 별도 락 키를 없애고 Lua로 원자화**

처음에는 `lock:{licenseId}`라는 락 키를 따로 TTL을 주고 두는 방식이었는데, 예외 상황에서 남는 락을 관리해야 하는 부담이 있었고,<br>
사실 `SET NX`를 `license:{licenseId}`에 걸어도 됐었다. (`lock:{licenseId}` 는 필요가 없었음)

세션 하나를 만들 때 실제로는 네 개의 Redis 키(세션 본체 · 라이센스 역참조 · TTL 트리거 · 시퀀스)를 저장하는데, 네 개의 키를 저장하던 중 예외가 발생하면 부분적으로 저장돼 정합성이 깨진다는 문제도 함께 있었다.

그래서 세션 저장 전체를 Lua 스크립트 한 번으로 원자화하고, 그 안에서 라이센스 역참조 키(`license:{id}`)를 SETNX로 선점하도록 바꿨다.
별도 락 키 없이 저장 자체가 선점을 겸하게 되면서 락 키 해제 문제도 같이 사라졌다.
</details>

<br>
<details>
<summary><b>세션 목록 조회 Redis N+1</b> - 왕복 2N → 2, 페이지 크기 100 기준 5배 (134ms → 27ms)</summary>

→ 상세: [세션 목록 Redis N+1 제거](https://potential-3.tistory.com/17)


**증상**

소프트웨어별 세션 목록 API가 페이지 크기를 키울수록 눈에 띄게 느려졌다.
크기를 10배 늘리면 응답 시간도 거의 10배로 늘어나는 선형 증가였다.

<br>

**원인 - 페이지 크기만큼 늘어나는 Redis 왕복**

세션 정보는 라이센스마다 두 번의 조회를 거쳐야 한다.

```
license:{licenseId}  → sessionId
session:{sessionId}  → 세션 JSON
```

이를 라이센스마다 반복하고 있었다. 페이지 크기가 N이면 Redis 왕복이 **2N번** 발생한다.
개발 초기에 "Redis는 빠르고 페이지 크기도 100 이하일 테니 괜찮겠지"하고 판단하고 넘긴 부분이었다.

<br>

**조치 - MGET으로 왕복을 2회에 고정**

서비스 계층에서 라이센스 ID를 먼저 모으고, 저장소 계층에서 MGET 두 번으로 끝내도록 바꿨다.

```
1) MGET license:{id1} license:{id2} ...   → sessionId 목록
2) MGET session:{sid1} session:{sid2} ... → 세션 JSON 목록
```

라이센스가 몇 개든 왕복은 2회로 고정된다.

| 페이지 크기 | Redis 왕복 | median (전 → 후) | 배수 |
|---|---|---|---|
| 100 | 200 → 2 | 0.134s → 0.027s | 5.0배 |
| 1000 | 2000 → 2 | 1.086s → 0.077s | 14.1배 |


- 로컬 측정이라 네트워크 왕복 비용이 빠져 있다. Redis가 분리된 운영 환경에서는 왕복 하나하나에 지연이 붙으므로 차이가 더 벌어진다.

**배운 것** - N+1은 ORM만의 문제가 아니라 외부 저장소와 직접 왕복하는 모든 구조에서 생긴다.

</details>

<br>
<details>
<summary><b>QueryDSL DATE() 결과 LocalDate 바인딩 실패</b> - 컴파일은 통과하고 런타임에 깨진 타입 힌트</summary>

→ 상세: [issue #1 - QueryDSL DATE() 바인딩 실패](https://github.com/koza-d/license-management-service/issues/1)

<br>

**증상**

일자별 통계 쿼리에서 `DATE()` 결과를 `LocalDate`로 받아 DTO를 만들려 했는데,
IDE에서는 아무 오류가 없다가 실행하는 순간 `argument type mismatch`로 깨졌다.

```java
DateTemplate<LocalDate> dateOnly =
    Expressions.dateTemplate(LocalDate.class, "DATE({0})", sessionLog.verifyAt);
```
<br>

**원인 - 타입 인자는 약속이 아니라 힌트다**

`dateTemplate`에 넘긴 `LocalDate.class`는 QueryDSL이 컴파일 시점에 참고하는 힌트일 뿐,
DB가 무엇을 돌려줄지 강제하지 못한다.
실제로 MySQL 드라이버는 `java.sql.Date`를 반환했고, 그 값이 `LocalDate`를 받는 생성자로 들어가면서 깨졌다.
컴파일러는 이 불일치를 볼 수 없다.

<br>

**조치 - 드라이버가 주는 타입으로 받고 한 곳에서 변환**

```java
DateTemplate<java.sql.Date> dateOnly =
    Expressions.dateTemplate(java.sql.Date.class, "DATE({0})", sessionLog.verifyAt);
```

DTO 생성자에서 `date.toLocalDate()`로 변환하도록 해서, 변환 지점을 한 곳으로 모았다.
<br>

**배운 것** - 네이티브 함수를 끼워 넣을 때는 원하는 타입을 선언할 게 아니라
드라이버가 실제로 주는 타입으로 받고 한 지점에서 변환하는 편이 안전하다.


</details>
