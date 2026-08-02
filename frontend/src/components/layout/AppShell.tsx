import { NavLink, Outlet } from "react-router-dom";
import { useAuthStore } from "../../stores/authStore";

const NAV_ITEMS = [
    { to: "/", label: "Repositories" },
    { to: "/search", label: "Search" },
    { to: "/ask", label: "Ask Ariadne" },
];

export default function AppShell() {
    const user = useAuthStore((s) => s.user);
    const logout = useAuthStore((s) => s.logout);

    return (
        <div className="flex min-h-screen">
            <aside className="w-56 shrink-0 border-r border-line bg-surface px-4 py-6">
                <div className="mb-8 flex items-baseline gap-2 px-2">
                    <span className="h-px w-4 bg-thread" />
                    <span className="font-display text-lg text-paper">Ariadne</span>
                </div>

                <nav className="space-y-1">
                    {NAV_ITEMS.map((item) => (
                        <NavLink
                            key={item.to}
                            to={item.to}
                            end={item.to === "/"}
                            className={({ isActive }) =>
                                `relative block rounded-md px-3 py-2 text-sm transition ${
                                    isActive ? "text-paper" : "text-fog hover:text-paper"
                                }`
                            }
                        >
                            {({ isActive }) => (
                                <>
                                    {/* The signature element: a thin gold thread marking the
                      active path, not a filled highlight block. */}
                                    {isActive && (
                                        <span className="absolute left-0 top-1/2 h-4 w-0.5 -translate-y-1/2 rounded bg-thread" />
                                    )}
                                    <span className={isActive ? "pl-2" : ""}>{item.label}</span>
                                </>
                            )}
                        </NavLink>
                    ))}
                </nav>

                <div className="absolute bottom-6 left-4 right-4">
                    <div className="border-t border-line pt-3 text-xs text-fog">
                        <p className="truncate font-mono">{user?.email}</p>
                        <button onClick={logout} className="mt-1 text-fog underline-offset-2 hover:text-paper hover:underline">
                            Sign out
                        </button>
                    </div>
                </div>
            </aside>

            <main className="flex-1 overflow-y-auto px-8 py-6">
                <Outlet />
            </main>
        </div>
    );
}