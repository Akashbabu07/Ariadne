import { create } from "zustand";
import type { User } from "../lib/types";

interface AuthState {
    accessToken: string | null;
    refreshToken: string | null;
    user: User | null;
    setSession: (accessToken: string, refreshToken: string) => void;
    setUser: (user: User) => void;
    logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
    accessToken: localStorage.getItem("ariadne_access_token"),
    refreshToken: localStorage.getItem("ariadne_refresh_token"),
    user: null,
    setSession: (accessToken, refreshToken) => {
        localStorage.setItem("ariadne_access_token", accessToken);
        localStorage.setItem("ariadne_refresh_token", refreshToken);
        set({ accessToken, refreshToken });
    },
    setUser: (user) => set({ user }),
    logout: () => {
        localStorage.removeItem("ariadne_access_token");
        localStorage.removeItem("ariadne_refresh_token");
        set({ accessToken: null, refreshToken: null, user: null });
    },
}));