package com.Ariadne.project.security;

import java.util.UUID;

public class RequestContext {
    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> ORG_ID = new ThreadLocal<>();

    private static final ThreadLocal<java.util.List<String>> ROLES = new ThreadLocal<>();
    public static void set(UUID userId, UUID orgId, java.util.List<String> roles) {
        USER_ID.set(userId);
        ORG_ID.set(orgId);
        ROLES.set(roles);
    }

    public static java.util.List<String> currentRoles() {
        return ROLES.get() != null ? ROLES.get() : java.util.List.of();
    }

    public static UUID currentUserId() {
        return USER_ID.get();
    }

    public static UUID currentOrgId() {
        return ORG_ID.get();
    }

    public static void clear() {
        USER_ID.remove();
        ROLES.remove();
        ORG_ID.remove();
    }

}
