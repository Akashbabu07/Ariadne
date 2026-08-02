const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export interface ApiResponse<T> {
    success: boolean;
    data: T;
    error?: { code: string; message: string };
}

class ApiError extends Error {
    public status: number;

    public code: string;

    constructor(status: number, code: string, message: string) {
        super(message);
        this.code = code;
        this.status = status;
    }
}

let refreshPromise: Promise<boolean> | null = null;

async function tryRefresh(): Promise<boolean> {

    if (!refreshPromise) {
        refreshPromise = (async () => {
            const { refreshToken, setSession, logout } = useAuthStore.getState();
            if (!refreshToken) return false;

            try {
                const res = await fetch(`${API_BASE}/api/v1/auth/refresh`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ refreshToken }),
                });
                const body: ApiResponse<{ accessToken: string; refreshToken: string }> = await res.json();
                if (!res.ok || !body.success) {
                    logout();
                    return false;
                }
                setSession(body.data.accessToken, body.data.refreshToken);
                return true;
            } catch {
                logout();
                return false;
            }
        })().finally(() => {
            refreshPromise = null;
        });
    }
    return refreshPromise;
}

async function request<T>(path: string, options: RequestInit = {}, isRetry = false): Promise<T> {
    const token = useAuthStore.getState().accessToken;

    const res = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
            ...options.headers,
        },
    });

    const body: ApiResponse<T> = await res.json();

    if (!res.ok || !body.success) {
        if (res.status === 401 && !isRetry) {
            const refreshed = await tryRefresh();
            if (refreshed) {

                return request<T>(path, options, true);
            }
            window.location.href = "/login";
        }
        throw new ApiError(res.status, body.error?.code ?? "UNKNOWN", body.error?.message ?? "Request failed");
    }

    return body.data;
}

export const api = {
    get: <T>(path: string) => request<T>(path),
    post: <T>(path: string, body?: unknown) =>
        request<T>(path, { method: "POST", body: body ? JSON.stringify(body) : undefined }),
    put: <T>(path: string, body?: unknown) =>
        request<T>(path, { method: "PUT", body: body ? JSON.stringify(body) : undefined }),
};

import { useAuthStore } from "../stores/authStore";