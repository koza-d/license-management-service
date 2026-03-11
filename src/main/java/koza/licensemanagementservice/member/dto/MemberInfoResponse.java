package koza.licensemanagementservice.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MemberInfoResponse {
    private String email;
    private String nickname;
    private List<String> roles;
}
