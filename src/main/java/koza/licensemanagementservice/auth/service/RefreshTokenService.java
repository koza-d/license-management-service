package koza.licensemanagementservice.auth.service;

import koza.licensemanagementservice.auth.dto.JwtTokenDTO;
import koza.licensemanagementservice.auth.jwt.JwtTokenProvider;
import koza.licensemanagementservice.auth.repository.RefreshTokenRepository;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    public JwtTokenDTO refreshToken(String refreshToken) {
        Long memberId = refreshTokenRepository.findMemberIdByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (member.getStatus() == MemberStatus.SUSPENDED) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.MEMBER_SUSPENDED);
        }

        refreshTokenRepository.delete(refreshToken);
        return jwtTokenProvider.createToken(member);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }
}
