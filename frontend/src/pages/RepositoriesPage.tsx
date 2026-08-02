import { useState } from "react";
import { useAuthStore } from "../stores/authStore";
import {
    useOrganization,
    useProjects,
    useCreateProject,
} from "../hooks/useOrganization";
import { useRepositories } from "../hooks/useRepositories";
import { RepositoryCard } from "../components/RepositoryCard";
import { NewRepositoryForm } from "../components/NewRepositoryForm";
import { RepositoryCardSkeleton } from "../components/Skeleton";

export default function RepositoriesPage() {
    const user = useAuthStore((s) => s.user);
    const { data: org } = useOrganization(user?.orgId);
    const { data: projects } = useProjects(user?.orgId);

    const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);
    const [showNewProject, setShowNewProject] = useState(false);
    const [showNewRepo, setShowNewRepo] = useState(false);

    const activeProjectId = selectedProjectId ?? projects?.[0]?.id;

    const {
        data: repos,
        isLoading: reposLoading,
    } = useRepositories(activeProjectId);

    const createProject = useCreateProject(user?.orgId);

    return (
        <div>
            <div className="mb-1 flex items-baseline gap-2">
                <span className="h-px w-4 bg-thread" />
                <p className="font-mono text-xs uppercase tracking-wide text-fog">
                    {org?.name ?? "…"}
                </p>
            </div>

            <h1 className="font-display text-2xl text-paper">
                Repositories
            </h1>

            {/* Project selector */}
            <div className="mt-6 flex flex-wrap items-center gap-2">
                {projects?.map((p) => (
                    <button
                        key={p.id}
                        onClick={() => setSelectedProjectId(p.id)}
                        className={`rounded-md border px-3 py-1.5 text-sm transition ${
                            p.id === activeProjectId
                                ? "border-thread text-paper"
                                : "border-line text-fog hover:text-paper"
                        }`}
                    >
                        {p.name}
                    </button>
                ))}

                <button
                    onClick={() => setShowNewProject((v) => !v)}
                    className="rounded-md border border-dashed border-line px-3 py-1.5 text-sm text-fog hover:border-thread hover:text-paper"
                >
                    + New project
                </button>
            </div>

            {showNewProject && (
                <form
                    className="mt-3 flex gap-2"
                    onSubmit={(e) => {
                        e.preventDefault();

                        const name = (
                            e.currentTarget.elements.namedItem(
                                "name"
                            ) as HTMLInputElement
                        ).value;

                        createProject.mutate(
                            { name },
                            {
                                onSuccess: () => setShowNewProject(false),
                            }
                        );
                    }}
                >
                    <input
                        name="name"
                        required
                        placeholder="Project name"
                        className="rounded-md border border-line bg-surface px-3 py-2 text-sm text-paper outline-none focus:border-thread"
                    />

                    <button
                        type="submit"
                        className="rounded-md bg-thread px-4 py-2 text-sm font-medium text-ink"
                    >
                        Create
                    </button>
                </form>
            )}

            {/* Repository list */}
            {activeProjectId && (
                <div className="mt-8">
                    <div className="mb-3 flex items-center justify-between">
                        <h2 className="text-sm text-fog">Repositories</h2>

                        <button
                            onClick={() => setShowNewRepo((v) => !v)}
                            className="text-sm text-thread hover:underline"
                        >
                            + Register repository
                        </button>
                    </div>

                    {showNewRepo && (
                        <div className="mb-4">
                            <NewRepositoryForm
                                projectId={activeProjectId}
                                onDone={() => setShowNewRepo(false)}
                            />
                        </div>
                    )}

                    {!reposLoading && repos?.length === 0 && (
                        <p className="rounded-lg border border-dashed border-line p-6 text-center text-sm text-fog">
                            No repositories yet — register one above to start
                            building engineering intelligence for this project.
                        </p>
                    )}

                    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
                        {reposLoading
                            ? Array.from({ length: 3 }).map((_, i) => (
                                <RepositoryCardSkeleton key={i} />
                            ))
                            : repos?.map((repo) => (
                                <RepositoryCard
                                    key={repo.id}
                                    repo={repo}
                                />
                            ))}
                    </div>
                </div>
            )}

            {!activeProjectId && projects?.length === 0 && (
                <p className="mt-8 rounded-lg border border-dashed border-line p-6 text-center text-sm text-fog">
                    No projects yet — create one above to register your first
                    repository.
                </p>
            )}
        </div>
    );
}