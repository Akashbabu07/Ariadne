import {type FormEvent, useState } from "react";
import { useRegisterRepository } from "../hooks/useRepositories";

export function NewRepositoryForm({ projectId, onDone }: { projectId: string; onDone: () => void }) {
    const [gitUrl, setGitUrl] = useState("");
    const register = useRegisterRepository(projectId);

    function handleSubmit(e: FormEvent) {
        e.preventDefault();
        register.mutate({ gitUrl }, { onSuccess: () => { setGitUrl(""); onDone(); } });
    }

    return (
        <form onSubmit={handleSubmit} className="flex gap-2">
            <input
                type="url"
                required
                placeholder="https://github.com/org/repo.git"
                value={gitUrl}
                onChange={(e) => setGitUrl(e.target.value)}
                className="flex-1 rounded-md border border-line bg-surface px-3 py-2 font-mono text-sm text-paper outline-none focus:border-thread"
            />
            <button
                type="submit"
                disabled={register.isPending}
                className="rounded-md bg-thread px-4 py-2 text-sm font-medium text-ink transition hover:brightness-110 disabled:opacity-50"
            >
                {register.isPending ? "Registering…" : "Register"}
            </button>
            {register.isError && <p className="text-sm text-risk">Couldn't register that repository.</p>}
        </form>
    );
}