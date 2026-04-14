package koza.licensemanagementservice.domain.member.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.member.dto.response.AdminMemberDetailResponse;
import koza.licensemanagementservice.domain.member.dto.response.AdminMemberSummaryResponse;
import koza.licensemanagementservice.domain.member.dto.request.MemberGradeChangeRequest;
import koza.licensemanagementservice.domain.member.dto.request.MemberStatusChangeRequest;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberGrade;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.member.log.dto.MemberGradeChangedEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberStatusChangedEvent;
import koza.licensemanagementservice.domain.member.log.dto.response.MemberLogResponse;
import koza.licensemanagementservice.domain.member.log.entity.MemberLog;
import koza.licensemanagementservice.domain.member.log.entity.MemberLogType;
import koza.licensemanagementservice.domain.member.log.repository.MemberLogRepository;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAdminService {
    private final MemberRepository memberRepository;
    private final MemberLogRepository memberLogRepository;
    private final ApplicationEventPublisher publisher;

    public Page<AdminMemberSummaryResponse> getMembers(String keyword, MemberStatus status, Pageable pageable) {
        return memberRepository.searchForAdmin(keyword, status, pageable)
                .map(AdminMemberSummaryResponse::from);
    }

    public AdminMemberDetailResponse getMemberDetail(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return AdminMemberDetailResponse.from(member);
    }

    public List<MemberLogResponse> getLogs(Long memberId, MemberLogType type) {
        List<MemberLog> logs = (type == null)
                ? memberLogRepository.findByMemberIdOrderByCreateAtDesc(memberId)
                : memberLogRepository.findByMemberIdAndLogTypeOrderByCreateAtDesc(memberId, type);
        return logs.stream().map(MemberLogResponse::from).toList();
    }

    @Transactional
    public void changeStatus(CustomUser admin, Long memberId, MemberStatusChangeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Member manager = memberRepository.findById(admin.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == request.getAction()) {
            throw new BusinessException(ErrorCode.MEMBER_STATUS_SAME);
        }

        MemberStatus before = member.getStatus();
        member.changeStatus(request.getAction());

        publisher.publishEvent(MemberStatusChangedEvent.builder()
                .target(member)
                .operator(manager)
                .before(before)
                .after(request.getAction())
                .reason(request.getReason())
                .build());
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
}
