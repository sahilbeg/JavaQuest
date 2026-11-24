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

/** The editor language for a file: Java, JavaScript (also JSX), CSS or HTML. */
export function languageFor(path = "") {
  const ext = path.split(".").pop().toLowerCase();
  return { java: "java", js: "javascript", jsx: "javascript", mjs: "javascript", css: "css", html: "html" }[ext] ?? "plaintext";
}

/** Colours Java source as HTML. Waits for Monaco to load the Java tokenizer, which happens lazily. */
export async function colorizeJava(monaco, source) {
  return colorize(monaco, source, "java");
}

/** Colours source in any editor language as HTML. */
export async function colorize(monaco, source, language) {
  monaco.editor.setTheme(editorTheme());
  monaco.editor.createModel("", language).dispose();
  const tabSize = language === "java" ? 4 : 2;
  for (let attempt = 0; attempt < 20; attempt++) {
    const html = await monaco.editor.colorize(source, language, { tabSize });
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
      { token: "keyword", foreground: "4f46e5" },
      { token: "string", foreground: "15803d" },
      { token: "comment", foreground: "6b6b75", fontStyle: "italic" },
      { token: "number", foreground: "b45309" },
      { token: "type", foreground: "7c3aed" },
    ],
    colors: {
      "editor.background": "#f7f7f8",
      "editor.lineHighlightBackground": "#efeff2",
      "editorLineNumber.foreground": "#77777f",
      "editorLineNumber.activeForeground": "#18181b",
      "editorCursor.foreground": "#4f46e5",
    },
  });
  monaco.editor.defineTheme("spoke-dark", {
    base: "vs-dark",
    inherit: true,
    rules: [
      { token: "keyword", foreground: "a5b4fc" },
      { token: "string", foreground: "86efac" },
      { token: "comment", foreground: "8b8b95", fontStyle: "italic" },
      { token: "number", foreground: "fcd34d" },
      { token: "type", foreground: "c4b5fd" },
    ],
    colors: {
      "editor.background": "#141417",
      "editor.lineHighlightBackground": "#1c1c21",
      "editorLineNumber.foreground": "#77777f",
      "editorLineNumber.activeForeground": "#ededef",
      "editorCursor.foreground": "#a5b4fc",
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
