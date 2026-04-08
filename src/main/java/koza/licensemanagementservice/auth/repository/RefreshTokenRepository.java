package koza.licensemanagementservice.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public void save(String refreshToken, Long memberId, long durationInSeconds) {
        redisTemplate.opsForValue().set(
                "RT:" + refreshToken,
                String.valueOf(memberId),
                Duration.ofMillis(durationInSeconds)
        );
    }

    // 토큰으로 사용자 ID 찾기
    public Optional<Long> findMemberIdByToken(String refreshToken) {
        String value = redisTemplate.opsForValue().get("RT:" + refreshToken);
        return Optional.ofNullable(value).map(Long::valueOf);
    }

    // 로그아웃 시 토큰 삭제
    public void delete(String refreshToken) {
        redisTemplate.delete("RT:" + refreshToken);
    }

}
