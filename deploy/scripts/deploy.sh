#!/bin/bash

set -e

cd /home/ubuntu/app/deploy

if [ -n "$IMAGE_TAG" ]; then
    sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=$IMAGE_TAG/" .env
fi

CURRENT=$(grep -oP 'set \$upstream app-\K(blue|green)' nginx/nginx.conf)

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
until [ "$(docker inspect -f '{{.State.Health.Status}}' "$(docker compose ps -q app-$TARGET)" 2>/dev/null)" = "healthy" ]; do
    sleep 2
done
echo "app-$TARGET is healthy"

sed -i "s/app-$CURRENT/app-$TARGET/" nginx/nginx.conf

docker compose exec -T nginx nginx -s reload
echo "Traffic switched: $CURRENT -> $TARGET"

docker image prune -f

echo "Smoke test..."
for i in $(seq 1 10); do
  code=$(curl -s -o /dev/null -w '${http_code}' https://api.licensify.kr/actuator/health || true)
  if [ "$code" = "200" ]; then
    echo "Smoke test 통과 (HTTP $code)"
    exit 0
  fi
  echo "시도 $i/10: HTTP $code - 2초 후 재시도"
  sleep 2
done

echo "Smoke test 실패: HTTP Code 200 X, 트래픽 $CURRENT 로 롤백"
sed -i "s/app-$TARGET/app-$CURRENT/" nginx/nginx.conf
docker compose exe -T nginx nginx -s reload