-- KEYS[1] = session:{sessionId}
-- KEYS[2] = license:{licenseId}
-- KEYS[3] = trigger:{sessionId}
-- KEYS[4] = seq:{sessionId}
-- ARGV[1] = sessionId

-- 실제 license: 에 맵핑된 sessionId가 전달받은 sessionId와 다르면
-- 기존 세션을 밀어내고 새로운 세션이 차지하고 있는 상태이므로 license: 키는 제거 X

redis.call('DEL', KEYS[1])
redis.call('DEL', KEYS[3])
redis.call('DEL', KEYS[4])

local real_session_id = redis.call('GET', KEYS[2])
if real_session_id == ARGV[1] then
    redis.call('DEL', KEYS[2])
end
