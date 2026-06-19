#!/bin/bash
set -euo pipefail

CONTAINER=deploy-mysql-1
DB_NAME=licensify
S3_BUCKET=s3://amzn-s3-licensify-db-backup/mysql
ENV_FILE=/home/ubuntu/app/deploy/.env

source "$ENV_FILE"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
FILE="mysql_${DB_NAME}_${TIMESTAMP}.sql.gz"
TMP="/tmp/${FILE}"

docker exec -e MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" "$CONTAINER" \
  mysqldump -u root \
  --single-transaction --quick --routines --triggers \
  "$DB_NAME" | gzip > "$TMP"

aws s3 cp "$TMP" "${S3_BUCKET}/${FILE}"
rm -f "$TMP"
echo "[$(date)] backup ok: ${FILE}"
curl -fsS -m 10 https://hc-ping.com/89222c6f-cf90-44f6-bcdb-8d674da26935
