#!/usr/bin/env bash
# /opt/lms/scripts/backup.sh
# cron: 매일 02:00 KST 실행 권장
#   crontab -e
#   0 2 * * * /opt/lms/scripts/backup.sh >> /var/log/lms-backup.log 2>&1

set -euo pipefail

cd /opt/lms
# shellcheck disable=SC1091
source ./.env

TS="$(date +%Y%m%d-%H%M%S)"
OBJECT="lms-${TS}.sql.gz.gpg"

echo "[$(date -Iseconds)] backup start → s3://${BACKUP_BUCKET}/${OBJECT}"

# 일회용 mysql 클라이언트 컨테이너로 덤프 → gzip → gpg 암호화 → S3 업로드
docker run --rm \
  -e MYSQL_PWD="${DB_PASSWORD}" \
  mysql:8.0 \
  mysqldump \
    --host="${DB_HOST}" \
    --port="${DB_PORT:-3306}" \
    --user="${DB_USERNAME}" \
    --single-transaction \
    --quick \
    --set-gtid-purged=OFF \
    --default-character-set=utf8mb4 \
    "${DB_NAME}" \
  | gzip -9 \
  | gpg --batch --yes --symmetric --cipher-algo AES256 \
        --passphrase "${BACKUP_GPG_PASSPHRASE}" \
  | aws s3 cp - "s3://${BACKUP_BUCKET}/${OBJECT}" \
        --region "${AWS_REGION}" \
        --storage-class STANDARD_IA \
        --expected-size $((1024 * 1024 * 200))

echo "[$(date -Iseconds)] backup done"

# 복호화 예시 (디스크에 임시로 받는 경우):
#   aws s3 cp s3://${BACKUP_BUCKET}/${OBJECT} - \
#     | gpg --batch --passphrase "${BACKUP_GPG_PASSPHRASE}" --decrypt \
#     | gunzip > restore.sql
