#!/bin/bash

set -e

# ===== Licensify blue-green 배포 (stop 전략) =====
# 실행: bash scripts/deploy.sh (반드시 ~/app/deploy 에서 — .env 자동 로딩 위치)
# IMAGE_TAG 환경변수로 새 이미지 태그 전달 (GitHub Actions가 SSH로 export)

CADDY_FILE="caddy/Caddyfile"
HEALTH_URL="https://api.licensify.kr/actuator/health"

cd /home/ubuntu/app/deploy

if [ -n "$IMAGE_TAG" ]; then
    sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=$IMAGE_TAG/" .env
fi

CURRENT=$(grep -oP 'reverse_proxy app-\K(blue|green)' ${CADDY_FILE})

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
for i in $(seq 1 30); do
  status=$(docker inspect -f '{{.State.Health.Status}}' "$(docker compose ps -q app-$TARGET)" 2>/dev/null || echo "starting")
  if [ "$status" = "healthy" ]; then
    echo "app-$TARGET healthy"
    break
  fi
  if [ "$i" -eq 30 ]; then
    echo "app-$TARGET health failed (마지막 상태: $status). 전환 중단."
    docker compose stop "app-$TARGET"
    exit 1
  fi
  sleep 2
done

sed -i "s/app-$CURRENT/app-$TARGET/" "${CADDY_FILE}"
docker compose exec -T caddy caddy reload --config /etc/caddy/Caddyfile --force
echo "Traffic switched: $CURRENT -> $TARGET"

echo "Smoke test..."
ok=0
for i in $(seq 1 10); do
  code=$(curl -s -o /dev/null -w '%{http_code}' "${HEALTH_URL}" || true)
  if [ "$code" = "200" ]; then
    echo "Smoke test 통과 (HTTP $code)"
    ok=1
    break
  fi
  echo "시도 $i/10: HTTP $code - 2초 후 재시도"
  sleep 2
done

if [ "${ok}" -ne 1 ]; then
  echo "Smoke test 실패: HTTP Code 200 X, 트래픽 $CURRENT 로 롤백"

  sed -i "s/app-$TARGET/app-$CURRENT/" "${CADDY_FILE}"
  docker compose exec -T caddy caddy reload --config /etc/caddy/Caddyfile --force
  docker compose stop "app-$TARGET"
  exit 1
fi

docker compose stop "app-$CURRENT"
echo "app-$CURRENT stop. live: app-${TARGET}"
docker image prune -f