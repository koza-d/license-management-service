package koza.licensemanagementservice.session.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class TerminateBulkRequest {
    private List<String> ids;
}
