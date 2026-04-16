package koza.licensemanagementservice.global.validation;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;

public class ValidUserAuthorized {
    public static void validAdminAuthorized(CustomUser user) {
        if (user == null)
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        if (user.getAuthorities() == null)
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

        if (!isAdmin)
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
}
