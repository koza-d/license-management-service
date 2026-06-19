#!/usr/bin/env bash
set -euo pipefail

# ===== Licensify 수동 롤백 (stop 전략 전제) =====
# 용도: 배포 성공(smoke 통과) 후 새 버전 문제를 발견했을 때.
# deploy.sh가 옛 버전을 stop 해뒀으므로, 그걸 다시 start + 트래픽 되돌림.
# 실행: bash scripts/rollback.sh    (반드시 ~/app/deploy 에서)

cd "$(dirname "$0")/.."

CADDY_FILE="caddy/Caddyfile"
HEALTH_URL="https://api.licensify.kr/actuator/health"

# 현재 라이브 / 되돌릴 대상
LIVE=$(grep -oP 'reverse_proxy app-\K(blue|green)' "$CADDY_FILE")
if [ "$LIVE" = "blue" ]; then PREV=green; else PREV=blue; fi
echo "현재 라이브=$LIVE  되돌릴 대상=$PREV"

# 이전 버전 재기동 (start: 기존 stopped 컨테이너를 그대로 = 옛 이미지 유지)
echo "app-$PREV 재기동..."
docker compose start "app-$PREV"

# health 대기
echo "app-$PREV healthy 대기..."
for i in $(seq 1 30); do
  status=$(docker inspect -f '{{.State.Health.Status}}' "$(docker compose ps -q app-$PREV)" 2>/dev/null || echo "starting")
  if [ "$status" = "healthy" ]; then
    echo "app-$PREV healthy"
    break
  fi
  if [ "$i" -eq 30 ]; then
    echo "app-$PREV health 실패 (마지막 상태: $status). 롤백 중단 — 라이브는 여전히 $LIVE."
    exit 1
  fi
  sleep 2
done

# 트래픽 되돌리기
sed -i "s/app-$LIVE/app-$PREV/" "$CADDY_FILE"
docker compose exec -T caddy caddy reload --config /etc/caddy/Caddyfile --force
echo "트래픽 롤백: $LIVE -> $PREV"

# 확인
code=$(curl -s -o /dev/null -w '%{http_code}' "$HEALTH_URL" || true)
echo "헬스: HTTP $code"

# 방금 내린(문제 있던) 버전 stop = 메모리 회수
docker compose stop "app-$LIVE"
echo "롤백 완료. app-$LIVE stop. 라이브: app-$PREV"