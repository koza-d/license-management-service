package koza.licensemanagementservice.domain.qna.dto.request;

import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QnaAdminSearchCondition {
    private List<QnaStatus> status;
    private String q;
    private String title;
    private String authorEmail;
    private String softwareName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdBefore;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime answeredAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime answeredBefore;

    public boolean hasAnyFieldFilter() {
        return title != null || authorEmail != null || softwareName != null;
    }

    public boolean hasFullTextFilter() {
        return q != null && !q.isBlank();
    }

    public void validateDateRanges() {
        if (createdAfter != null && createdBefore != null && createdAfter.isAfter(createdBefore)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (answeredAfter != null && answeredBefore != null && answeredAfter.isAfter(answeredBefore)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
