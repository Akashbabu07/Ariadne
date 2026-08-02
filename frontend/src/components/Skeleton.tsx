export function Skeleton({ className = "" }: { className?: string }) {
    return <div className={`animate-pulse rounded bg-line/60 ${className}`} />;
}

export function RepositoryCardSkeleton() {
    return (
        <div className="rounded-lg border border-line bg-surface p-4">
            <div className="flex items-start justify-between gap-4">
                <Skeleton className="h-4 w-32" />
                <Skeleton className="h-5 w-16" />
            </div>
            <div className="mt-4 flex gap-4">
                <Skeleton className="h-3 w-12" />
                <Skeleton className="h-3 w-16" />
            </div>
        </div>
    );
}