package koza.licensemanagementservice.domain.qna.dto.request;

import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import lombok.Data;

import java.util.List;

@Data
public class QnaAdminSearchCondition {
    private List<QnaStatus> status;
    private String q;
    private String title;
    private String authorEmail;
    private String softwareName;

    public boolean hasAnyFieldFilter() {
        return title != null || authorEmail != null || softwareName != null;
    }

    public boolean hasFullTextFilter() {
        return q != null && !q.isBlank();
    }
}
