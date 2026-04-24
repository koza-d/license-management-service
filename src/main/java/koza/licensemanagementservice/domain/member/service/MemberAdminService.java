package koza.licensemanagementservice.domain.member.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.member.dto.response.AdminMemberDetailResponse;
import koza.licensemanagementservice.domain.member.dto.response.AdminMemberSummaryResponse;
import koza.licensemanagementservice.domain.member.dto.request.MemberGradeChangeRequest;
import koza.licensemanagementservice.domain.member.dto.request.MemberRoleChangeRequest;
import koza.licensemanagementservice.domain.member.dto.request.MemberStatusChangeRequest;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberGrade;
import koza.licensemanagementservice.domain.member.entity.MemberRole;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.member.log.dto.MemberGradeChangedEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberRoleChangedEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberStatusChangedEvent;
import koza.licensemanagementservice.domain.member.log.dto.response.MemberLogResponse;
import koza.licensemanagementservice.domain.member.log.entity.MemberLog;
import koza.licensemanagementservice.domain.member.log.entity.MemberLogType;
import koza.licensemanagementservice.domain.member.log.repository.MemberLogRepository;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.validAdminAuthorized;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberAdminService {
    private final MemberRepository memberRepository;
    private final MemberLogRepository memberLogRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public Page<AdminMemberSummaryResponse> getMembers(CustomUser admin, String keyword, MemberStatus status, Pageable pageable) {
        validAdminAuthorized(admin);
        return memberRepository.searchForAdmin(keyword, status, pageable)
                .map(AdminMemberSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public AdminMemberDetailResponse getMemberDetail(CustomUser admin, Long memberId) {
        validAdminAuthorized(admin);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return AdminMemberDetailResponse.from(member);
    }

    @Transactional(readOnly = true)
    public List<MemberLogResponse> getLogs(CustomUser admin, Long memberId, MemberLogType type) {
        validAdminAuthorized(admin);
        List<MemberLog> logs = (type == null)
                ? memberLogRepository.findByMemberIdOrderByCreateAtDesc(memberId)
                : memberLogRepository.findByMemberIdAndLogTypeOrderByCreateAtDesc(memberId, type);
        return logs.stream().map(MemberLogResponse::from).toList();
    }

    @Transactional
    public void changeStatus(CustomUser admin, Long memberId, MemberStatusChangeRequest request) {
        validAdminAuthorized(admin);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Member manager = memberRepository.findById(admin.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == request.getStatus()) {
            throw new BusinessException(ErrorCode.MEMBER_STATUS_SAME);
        }

        MemberStatus before = member.getStatus();
        member.changeStatus(request.getStatus());

        publisher.publishEvent(MemberStatusChangedEvent.builder()
                .target(member)
                .operator(manager)
                .before(before)
                .after(request.getStatus())
                .reason(request.getReason())
                .build());
    }

    @Transactional
    public void changeGrade(CustomUser admin, Long memberId, MemberGradeChangeRequest request) {
        validAdminAuthorized(admin);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Member manager = memberRepository.findById(admin.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getGrade() == request.getGrade()) {
            throw new BusinessException(ErrorCode.MEMBER_GRADE_SAME);
        }

        MemberGrade before = member.getGrade();
        member.changeGrade(request.getGrade());

        publisher.publishEvent(MemberGradeChangedEvent.builder()
                .target(member)
                .operator(manager)
                .before(before)
                .after(request.getGrade())
                .reason(request.getReason())
                .build());
    }

    @Transactional
    public void changeRole(CustomUser admin, Long memberId, MemberRoleChangeRequest request) {
        validAdminAuthorized(admin);
        if (admin.getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_ROLE_SELF_FORBIDDEN);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Member manager = memberRepository.findById(admin.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        MemberRole before = member.getRole();
        if (before == request.getRole()) {
            throw new BusinessException(ErrorCode.MEMBER_ROLE_SAME);
        }

        member.changeRole(request.getRole());

        publisher.publishEvent(MemberRoleChangedEvent.builder()
                .target(member)
                .operator(manager)
                .before(before)
                .after(request.getRole())
                .reason(request.getReason())
                .build());
    }
}
