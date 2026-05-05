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

    public void save(String refreshToken, Long memberId, long durationInMillis) {
        redisTemplate.opsForValue().set(
                "RT:" + refreshToken,
                String.valueOf(memberId),
                Duration.ofMillis(durationInMillis)
        );
        redisTemplate.opsForValue().set(
                "RT_I:" + memberId,
                refreshToken,
                Duration.ofMillis(durationInMillis)
        );
    }

    // 토큰으로 사용자 ID 찾기
    public Optional<Long> findMemberIdByToken(String refreshToken) {
        String value = redisTemplate.opsForValue().get("RT:" + refreshToken);
        return Optional.ofNullable(value).map(Long::valueOf);
    }

    // 사용자 ID로 토큰 찾기
    public Optional<String> findByMemberId(Long memberId) {
        String value = redisTemplate.opsForValue().get("RT_I:" + memberId);
        return Optional.ofNullable(value);
    }

    // 사용자 ID로 토큰 제거
    public void deleteByMemberId(Long memberId) {
        Optional<String> refreshTokenOpt = findByMemberId(memberId);
        if (refreshTokenOpt.isPresent()) {
            redisTemplate.delete("RT_I:" + memberId);
            redisTemplate.delete("RT:" + refreshTokenOpt.get());
        }
    }

    // 로그아웃 시 토큰 삭제
    public void delete(String refreshToken) {
        Optional<Long> memberIdOpt = findMemberIdByToken(refreshToken);
        if (memberIdOpt.isPresent()) {
            redisTemplate.delete("RT:" + refreshToken);
            redisTemplate.delete("RT_I:" + memberIdOpt.get());
        }
    }

}
