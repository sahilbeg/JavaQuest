import { bus } from "./events.js";

let loading;

/** Loads the vendored Monaco editor once and returns the monaco namespace. */
export function loadMonaco() {
  loading ??= new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = "/vendor/monaco/vs/loader.js";
    script.onload = () => {
      window.require.config({ paths: { vs: "/vendor/monaco/vs" } });
      window.require(["vs/editor/editor.main"], () => {
        defineThemes(window.monaco);
        window.monaco.editor.setTheme(editorTheme());
        bus.addEventListener("theme", () => window.monaco.editor.setTheme(editorTheme()));
        resolve(window.monaco);
      }, reject);
    };
    script.onerror = () => reject(new Error("The code editor could not load."));
    document.head.append(script);
  });
  return loading;
}

/** Colours Java source as HTML. Waits for Monaco to load the Java tokenizer, which happens lazily. */
export async function colorizeJava(monaco, source) {
  monaco.editor.setTheme(editorTheme());
  monaco.editor.createModel("", "java").dispose();
  for (let attempt = 0; attempt < 20; attempt++) {
    const html = await monaco.editor.colorize(source, "java", { tabSize: 4 });
    if (/class="mtk(?!1")/.test(html)) return html.replace(/(<br\/>)+$/, "");
    await new Promise((resolve) => setTimeout(resolve, 100));
  }
  return null;
}

export function isDark() {
  const theme = document.documentElement.dataset.theme;
  return theme === "dark" || (theme === "system" && matchMedia("(prefers-color-scheme: dark)").matches);
}

export function editorTheme() {
  return isDark() ? "spoke-dark" : "spoke-light";
}

function defineThemes(monaco) {
  monaco.editor.defineTheme("spoke-light", {
    base: "vs",
    inherit: true,
    rules: [
      { token: "keyword", foreground: "3046c8", fontStyle: "bold" },
      { token: "string", foreground: "17663a" },
      { token: "comment", foreground: "565e6f", fontStyle: "italic" },
      { token: "number", foreground: "8f3f18" },
      { token: "type", foreground: "7a3fb0" },
    ],
    colors: {
      "editor.background": "#fafbfc",
      "editor.lineHighlightBackground": "#eef1f8",
      "editorLineNumber.foreground": "#646b7a",
      "editorLineNumber.activeForeground": "#1d2230",
      "editorCursor.foreground": "#3046c8",
    },
  });
  monaco.editor.defineTheme("spoke-dark", {
    base: "vs-dark",
    inherit: true,
    rules: [
      { token: "keyword", foreground: "9fb1ff", fontStyle: "bold" },
      { token: "string", foreground: "8fd6a6" },
      { token: "comment", foreground: "8a90a0", fontStyle: "italic" },
      { token: "number", foreground: "f0b38a" },
      { token: "type", foreground: "d2a8ff" },
    ],
    colors: {
      "editor.background": "#1b1e26",
      "editor.lineHighlightBackground": "#232733",
      "editorLineNumber.foreground": "#9097a6",
      "editorLineNumber.activeForeground": "#e6e8ee",
      "editorCursor.foreground": "#9fb1ff",
    },
  });
}

export const EDITOR_OPTIONS = {
  language: "java",
  fontFamily: "JetBrains Mono, ui-monospace, monospace",
  fontSize: 14,
  lineHeight: 22,
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  automaticLayout: true,
  tabSize: 4,
  renderLineHighlight: "line",
  padding: { top: 12, bottom: 12 },
  scrollbar: { alwaysConsumeMouseWheel: false },
};
