import { useToastStore } from "../stores/toastStore";

export function Toaster() {
    const toasts = useToastStore((s) => s.toasts);
    const dismiss = useToastStore((s) => s.dismiss);

    return (
        <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
            {toasts.map((t) => (
                <div
                    key={t.id}
                    onClick={() => dismiss(t.id)}
                    className={`cursor-pointer rounded-md border px-4 py-2.5 text-sm shadow-lg backdrop-blur ${
                        t.variant === "success"
                            ? "border-safe/40 bg-surface text-safe"
                            : "border-risk/40 bg-surface text-risk"
                    }`}
                >
                    {t.message}
                </div>
            ))}
        </div>
    );
}