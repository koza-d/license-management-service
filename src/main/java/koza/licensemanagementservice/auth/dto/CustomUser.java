package koza.licensemanagementservice.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/*
 * SecurityContextHolder의 인증객체 속에 들어갈 유저 정보
 * JWT 클레임에 있는 데이터만 들어가있음. (DB 조회를 줄이기 위함)
 * 추가로 필요한 데이터는 DB를 통해 불러와야함
 */
@RequiredArgsConstructor
public class CustomUser implements UserDetails {
    @Getter
    private final Long id;
    @Getter
    private final String email;

    @Getter
    private final String nickname;
    private final List<SimpleGrantedAuthority> roles;

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
