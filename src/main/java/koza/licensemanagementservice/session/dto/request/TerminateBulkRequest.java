package koza.licensemanagementservice.session.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class TerminateBulkRequest {
    private List<String> ids;
}
