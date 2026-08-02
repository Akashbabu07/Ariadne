import { useState, type ReactNode } from "react";

export function Tabs({ tabs }: { tabs: { label: string; content: ReactNode }[] }) {
    const [active, setActive] = useState(0);

    return (
        <div>
            <div className="flex gap-1 border-b border-line">
                {tabs.map((tab, i) => (
                    <button
                        key={tab.label}
                        onClick={() => setActive(i)}
                        className={`relative px-4 py-2 text-sm transition ${
                            i === active ? "text-paper" : "text-fog hover:text-paper"
                        }`}
                    >
                        {tab.label}
                        {i === active && <span className="absolute inset-x-3 -bottom-px h-0.5 bg-thread" />}
                    </button>
                ))}
            </div>
            <div className="py-6">{tabs[active].content}</div>
        </div>
    );
}