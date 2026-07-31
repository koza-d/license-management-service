--- KEYS[1] = session:{sessionId}
--- KEYS[2] = license:{licenseId}
--- KEYS[3] = trigger:{sessionId}
--- KEYS[4] = seq:{sessionId}
--- ARGV[1~3] = SessionValue, sessionId, TTL(ms)

if redis.call('SETNX', KEYS[2], ARGV[2]) == 0 then
    return 0
end

redis.call('SET', KEYS[1], ARGV[1])
redis.call('SET', KEYS[3], '', 'PX', ARGV[3])
redis.call('SET', KEYS[4], '0', 'PX', ARGV[3])
return 1