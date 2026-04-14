package koza.licensemanagementservice.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import koza.licensemanagementservice.auth.dto.JwtTokenDTO;
import koza.licensemanagementservice.auth.repository.RefreshTokenRepository;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.auth.dto.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    public static final Long ACCESS_TOKEN_EXPIRY = 1000L * 60 * 60; // 1시간
    public static final Long REFRESH_TOKEN_EXPIRY = 14 * 24 * 60 * 60 * 1000L; // 14일
    
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    private SecretKey key;

    private final RefreshTokenRepository refreshTokenRepository;

    @PostConstruct
    protected void init() {
        // 기본적으로 private을 사용하는게 맞으나, 테스트 클래스에서 사용 불가, protected면 패키지를 맞추면 사용가능
        // 프레임워크가 제어하는 생명주기 메서드는 최소한의 열린 권한 부여(protected)
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public JwtTokenDTO createToken(Member member) {
        Claims claims = Jwts.claims().subject(member.getId().toString())
                .add("email", member.getEmail())
                .add("roles", member.getRoles())
                .add("nick", member.getNickname())
                .add("p_img", member.getProfileURL() == null ? "" : member.getProfileURL())
                .build();

        Date now = new Date();

        String accessToken = Jwts.builder()
                .claims(claims)
                .issuedAt(now) // 토큰 발행시간
                .expiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRY)) // 토큰 만료시간
                .signWith(key)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(member.getId().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRY))
                .signWith(key) // 추후 다른 키 권장
                .compact();

        refreshTokenRepository.save(refreshToken, member.getId(), REFRESH_TOKEN_EXPIRY);
        return new JwtTokenDTO(accessToken, refreshToken);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        // 토큰에 담긴 권한 추출
        List<?> roles = claims.get("roles", List.class);
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString()))
                .collect(Collectors.toList());

        CustomUser principal = new CustomUser(Long.parseLong(claims.getSubject()), claims.get("email").toString(), claims.get("nick").toString(), claims.get("p_img").toString(), authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
}
