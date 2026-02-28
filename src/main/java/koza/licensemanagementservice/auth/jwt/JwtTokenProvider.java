package koza.licensemanagementservice.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import koza.licensemanagementservice.member.entity.Member;
import koza.licensemanagementservice.member.dto.CustomUser;
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
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    private final Long tokenValidityInMs = 1000L * 60 * 60;
    private SecretKey key;

    @PostConstruct
    protected void init() {
        // 기본적으로 private을 사용하는게 맞으나, 테스트 클래스에서 사용 불가, protected면 패키지를 맞추면 사용가능
        // 프레임워크가 제어하는 생명주기 메서드는 최소한의 열린 권한 부여(protected)
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Member member) {
        Claims claims = Jwts.claims().subject(member.getEmail())
                .add("id", member.getId())
                .add("roles", member.getRoles())
                .add("nick", member.getNickname())
                .build();

        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMs);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now) // 토큰 발행시간
                .expiration(validity) // 토큰 만료시간
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        // 토큰에 담긴 권한 추출
        List<?> roles = claims.get("roles", List.class);
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.toString()))
                .collect(Collectors.toList());

        CustomUser principal = new CustomUser(claims.getSubject(), Long.parseLong(claims.get("id").toString()), claims.get("nick").toString(), authorities);
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
