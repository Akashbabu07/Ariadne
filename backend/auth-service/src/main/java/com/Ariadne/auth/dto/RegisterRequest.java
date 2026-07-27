package com.Ariadne.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterRequest(

        @NotNull(message = "orgId is required")
        UUID orgId,

        @NotNull @Email(message = "must be a valid email")
        String email,

        @NotNull
        @Size(min = 8, message = "password must be at least 8 characters")
        String password
) {}