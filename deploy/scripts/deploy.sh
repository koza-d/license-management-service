#!/bin/bash

set -e

cd /home/ubuntu/app

CURRENT=$(grep -oP 'proxy_pass http://app-\K(blue|green)' deploy/nginx.conf)

# 트래픽 안받는 쪽 = 업데이트 대상
if [ "$CURRENT" = "blue" ]; then
  TARGET="green"
else
  TARGET="blue"
fi

echo "현재 트래픽: $CURRENT / 업데이트 대상 : $TARGET"

docker compose pull "app-$TARGET"
docker compose up -d --no-deps "app-$TARGET"

echo "Waiting for app-$TARGET to be healthy..."
until [ "$(docker inspect -f '{{.State.Health.Status}}' "app-$TARGET")" ]; do
    sleep 2
done
echo "app-$TARGET is healthy"

sed -i "s/app-$CURRENT/app-$TARGET" nginx/nginx.conf

docker compose exec nginx nginx -s reload
echo "Traffic switched: $CURRENT -> $TARGET"

docker image prune -f

