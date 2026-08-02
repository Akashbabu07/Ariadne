import { useMutation } from "@tanstack/react-query";
import { api } from "../lib/api";
import { useAuthStore } from "../stores/authStore";
import type { User } from "../lib/types";

interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    expiresInSeconds: number;
}

export function useLogin() {
    const setSession = useAuthStore((s) => s.setSession);
    const setUser = useAuthStore((s) => s.setUser);

    return useMutation({
        mutationFn: (creds: { email: string; password: string }) =>
            api.post<AuthResponse>("/api/v1/auth/login", creds),
        onSuccess: async (data) => {
            setSession(data.accessToken, data.refreshToken);
            const user = await api.get<User>("/api/v1/auth/me");
            setUser(user);
        },
    });
}

export function useRegister() {
    return useMutation({
        mutationFn: (payload: { orgId: string; email: string; password: string }) =>
            api.post<User>("/api/v1/auth/register", payload),
    });
}