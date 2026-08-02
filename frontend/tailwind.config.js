/** @type {import('tailwindcss').Config} */
export default {
    content: ["./index.html", "./src/**/*.{ts,tsx}"],
    theme: {
        extend: {
            colors: {
                ink: "var(--ink)",
                surface: "var(--surface)",
                line: "var(--line)",
                thread: "var(--thread)",
                fog: "var(--fog)",
                paper: "var(--paper)",
                risk: "var(--risk)",
                safe: "var(--safe)",
            },
            fontFamily: {
                display: ["Fraunces", "serif"],
                sans: ["Inter", "sans-serif"],
                mono: ["JetBrains Mono", "monospace"],
            },
        },
    },
    plugins: [],
};