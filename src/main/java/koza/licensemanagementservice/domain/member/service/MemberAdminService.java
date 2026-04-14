package koza.licensemanagementservice.domain.member.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.member.log.dto.response.MemberGradeLogResponse;
import koza.licensemanagementservice.domain.member.log.dto.response.MemberStatusLogResponse;
import koza.licensemanagementservice.domain.member.log.entity.MemberGradeLog;
import koza.licensemanagementservice.domain.member.log.entity.MemberStatusLog;
import koza.licensemanagementservice.domain.member.log.repository.MemberGradeLogRepository;
import koza.licensemanagementservice.domain.member.log.repository.MemberStatusLogRepository;
import koza.licensemanagementservice.domain.member.dto.request.MemberGradeChangeRequest;
import koza.licensemanagementservice.domain.member.dto.request.MemberStatusChangeRequest;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAdminService {
    private final MemberRepository memberRepository;
    private final MemberStatusLogRepository memberStatusLogRepository;
    private final MemberGradeLogRepository memberGradeLogRepository;

    @Transactional
    public void changeStatus(CustomUser admin, Long memberId, MemberStatusChangeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Member manager = memberRepository.findById(admin.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == request.getAction()) {
            throw new BusinessException(ErrorCode.MEMBER_STATUS_SAME);
        }

        MemberStatusLog log = MemberStatusLog.builder()
                .member(member)
                .manager(manager)
                .action(request.getAction())
                .reason(request.getReason())
                .build();
        memberStatusLogRepository.save(log);

        member.changeStatus(request.getAction());
    }

    @Transactional
    public void changeGrade(CustomUser admin, Long memberId, MemberGradeChangeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Member manager = memberRepository.findById(admin.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getGrade() == request.getGrade()) {
            throw new BusinessException(ErrorCode.MEMBER_GRADE_SAME);
        }

        MemberGradeLog log = MemberGradeLog.builder()
                .member(member)
                .manager(manager)
                .previousGrade(member.getGrade())
                .newGrade(request.getGrade())
                .reason(request.getReason())
                .build();
        memberGradeLogRepository.save(log);

        member.changeGrade(request.getGrade());
    }

    public List<MemberStatusLogResponse> getStatusLogs(Long memberId) {
        return memberStatusLogRepository.findByMemberIdOrderByCreateAtDesc(memberId)
                .stream()
                .map(MemberStatusLogResponse::from)
                .toList();
    }

    public List<MemberGradeLogResponse> getGradeLogs(Long memberId) {
        return memberGradeLogRepository.findByMemberIdOrderByCreateAtDesc(memberId)
                .stream()
                .map(MemberGradeLogResponse::from)
                .toList();
    }
}
