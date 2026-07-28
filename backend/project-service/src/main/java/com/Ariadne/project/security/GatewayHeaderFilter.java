package com.Ariadne.project.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;


public class GatewayHeaderFilter  extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String userIdHeader = request.getHeader("X-User-Id");
            String orgIdHeader = request.getHeader("X-Org-Id");

            if (userIdHeader != null && orgIdHeader != null) {
                String rolesHeader = request.getHeader("X-User-Roles");
                         java.util.List<String> roles = rolesHeader != null
                                              ? java.util.Arrays.asList(rolesHeader.split(","))
                                                                           : java.util.List.of();
                                RequestContext.set(UUID.fromString(userIdHeader), UUID.fromString(orgIdHeader), roles);
            }

            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }
}
