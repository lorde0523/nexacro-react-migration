package com.lorde0523.migration.session;

import java.util.Set;

public record SessionVo(
        String userId,
        String userName,
        String companyCode,
        Set<String> roles
) {

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
