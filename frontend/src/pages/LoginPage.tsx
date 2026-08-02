import {type FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useLogin } from "../hooks/useAuth";

export default function LoginPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const login = useLogin();
    const navigate = useNavigate();

    function handleSubmit(e: FormEvent) {
        e.preventDefault();
        login.mutate(
            { email, password },
            { onSuccess: () => navigate("/") }
        );
    }

    return (
        <div className="flex min-h-screen items-center justify-center">
            <div className="w-full max-w-sm">
                <div className="mb-8 flex items-baseline gap-2">
                    <span className="h-px w-6 bg-thread" />
                    <h1 className="font-display text-2xl text-paper">Ariadne</h1>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="mb-1 block text-sm text-fog">Email</label>
                        <input
                            type="email"
                            required
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full rounded-md border border-line bg-surface px-3 py-2 text-paper outline-none focus:border-thread"
                        />
                    </div>
                    <div>
                        <label className="mb-1 block text-sm text-fog">Password</label>
                        <input
                            type="password"
                            required
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full rounded-md border border-line bg-surface px-3 py-2 text-paper outline-none focus:border-thread"
                        />
                    </div>

                    {login.isError && (
                        <p className="text-sm text-risk">
                            Couldn't sign in — check your email and password.
                        </p>
                    )}

                    <button
                        type="submit"
                        disabled={login.isPending}
                        className="w-full rounded-md bg-thread py-2 font-medium text-ink transition hover:brightness-110 disabled:opacity-50"
                    >
                        {login.isPending ? "Signing in…" : "Sign in"}
                    </button>
                </form>
            </div>
        </div>
    );
}