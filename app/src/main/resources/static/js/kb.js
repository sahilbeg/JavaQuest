import { h, icon } from "./dom.js";
import { api } from "./api.js";
import { loadMonaco, editorTheme, isDark, colorize, EDITOR_OPTIONS } from "./monaco.js";
import { bus } from "./events.js";
import { enhanceAnimations } from "./animate.js";

const CALLOUT_LABELS = { tip: "Tip", warning: "Careful", analogy: "Think of it like", jargon: "New word" };

/**
 * Turns rendered lesson HTML inside a container into the interactive version: callout labels, runnable
 * snippets, coloured Java, Mermaid diagrams and animations. Returns a function that cleans up.
 */
export function enhanceContent(container, chat) {
  container.classList.add("kb");
  container.querySelectorAll("aside.callout").forEach((aside) => {
    if (!aside.querySelector(".callout-label")) {
      aside.prepend(h("span", { class: "callout-label" }, CALLOUT_LABELS[aside.dataset.kind] ?? "Note"));
    }
  });
  container.querySelectorAll(".snippet").forEach((snippet) => {
    if (!snippet.querySelector(".snippet-bar")) enhanceSnippet(snippet, chat);
  });
  const disposers = [enhanceAnimations(container)];
  loadMonaco().then(async (monaco) => {
    const LANGS = { java: "java", js: "javascript", jsx: "javascript", javascript: "javascript", html: "html", css: "css" };
    for (const code of container.querySelectorAll(Object.keys(LANGS).map((l) => `pre > code.language-${l}`).join(", "))) {
      code.dataset.source ??= code.textContent;
      const html = await colorize(monaco, code.dataset.source, LANGS[code.dataset.lang] ?? "java");
      if (html && !code.closest(".snippet")?.dataset.open) code.innerHTML = html;
    }
  }).catch(() => {});
  renderDiagrams(container);
  const onTheme = () => renderDiagrams(container);
  bus.addEventListener("theme", onTheme);
  disposers.push(() => bus.removeEventListener("theme", onTheme));
  return () => disposers.forEach((d) => d());
}

/** Mermaid only understands hex and rgb colours, so theme tokens are converted through a canvas pixel. */
function cssColorToHex(color) {
  const ctx = document.createElement("canvas").getContext("2d", { willReadFrequently: true });
  ctx.fillStyle = color;
  ctx.fillRect(0, 0, 1, 1);
  const [r, g, b] = ctx.getImageData(0, 0, 1, 1).data;
  return `#${[r, g, b].map((v) => v.toString(16).padStart(2, "0")).join("")}`;
}

let mermaidLoading;
function renderDiagrams(container) {
  const diagrams = [...container.querySelectorAll("pre.mermaid")];
  if (!diagrams.length) return;
  mermaidLoading ??= new Promise((resolve, reject) => {
    const script = h("script", { src: "/vendor/mermaid/mermaid.min.js" });
    script.onload = () => resolve(window.mermaid);
    script.onerror = reject;
    document.head.append(script);
  });
  mermaidLoading.then(async (mermaid) => {
    const token = (name) => cssColorToHex(getComputedStyle(document.documentElement).getPropertyValue(name).trim());
    mermaid.initialize({
      startOnLoad: false,
      theme: "base",
      fontFamily: "Inter, sans-serif",
      themeVariables: {
        darkMode: isDark(),
        background: token("--surface"),
        primaryColor: token("--surface-2"),
        primaryBorderColor: token("--line-strong"),
        primaryTextColor: token("--ink"),
        lineColor: token("--ink-soft"),
        fontSize: "14px",
      },
    });
    for (const pre of diagrams) {
      pre.dataset.source ??= pre.textContent;
      pre.removeAttribute("data-processed");
      pre.textContent = pre.dataset.source;
    }
    await mermaid.run({ nodes: diagrams });
  }).catch(() => {});
}

function enhanceSnippet(snippet, chat) {
  const code = snippet.querySelector("code");
  const original = code.textContent;
  const lang = code.dataset.lang === "js" ? "js" : "java";
  const output = h("pre", { class: "snippet-output", hidden: true, "aria-live": "polite" });
  let editor;

  const runButton = h("button", { type: "button", class: "button button-primary", onclick: run }, icon("play"), "Run");
  const tryButton = h("button", { type: "button", class: "button", onclick: open }, icon("code"), "Try it yourself");
  const resetButton = h("button", { type: "button", class: "button button-quiet", hidden: true, onclick: reset }, icon("rotate-ccw"), "Reset");

  async function open() {
    const monaco = await loadMonaco();
    snippet.dataset.open = "true";
    const host = h("div", { class: "snippet-editor" });
    snippet.querySelector("pre").replaceWith(host);
    editor = monaco.editor.create(host, { ...EDITOR_OPTIONS, language: lang === "js" ? "javascript" : "java",
      tabSize: lang === "js" ? 2 : 4, value: original, theme: editorTheme() });
    tryButton.hidden = true;
    resetButton.hidden = false;
    editor.focus();
  }

  function reset() {
    editor?.setValue(original);
  }

  async function run() {
    runButton.setAttribute("aria-busy", "true");
    runButton.disabled = true;
    output.hidden = false;
    output.textContent = "Running…";
    try {
      const { result, explanations } = await api.runSnippet(editor ? editor.getValue() : original, lang);
      output.textContent = describeProgram(result, explanations);
      if (result.status === "COMPILE_ERROR") {
        chat.say(["That snippet didn't compile.", explanations.find(Boolean) ?? result.compileErrors[0]?.message],
          { mood: "encouraging" });
      }
    } catch (error) {
      output.textContent = error.message;
    } finally {
      runButton.removeAttribute("aria-busy");
      runButton.disabled = false;
    }
  }

  snippet.append(h("div", { class: "snippet-bar" }, runButton, tryButton, h("span", { class: "spacer" }), resetButton), output);
}

/**
 * Turns a run result into console text.
 * `input` says whether the page has a Program input box (`box`) and whether the learner typed anything in it (`given`).
 */
export function describeProgram(result, explanations = [], input = { box: false, given: false }) {
  switch (result.status) {
    case "COMPILE_ERROR":
      return result.compileErrors.map((e, i) =>
        `Line ${e.line}: ${explanations[i] || e.message}\n   ${/\.java$/.test(e.file ?? "") ? "Java" : "JavaScript"} says: ${e.message.split("\n")[0]}`).join("\n\n");
    case "TIMED_OUT":
      return `${result.output}\n\nStopped: the program ran for too long. Is there a loop that never ends?`.trim();
    case "EXCEPTION":
      return `${result.output}\n\n${inputHint(result.output, input) ?? "The program crashed. Read the first line above: it names the problem."}`.trim();
    default:
      return result.output.length ? result.output : "(The program finished without printing anything.)";
  }
}

/** A plain explanation when a crash came from reading input, or null for any other crash. */
function inputHint(output, input) {
  if (output.includes("java.util.InputMismatchException")) {
    return "The program read something it didn't expect, like a word where it wanted a number. "
      + (input.box ? "Check each line in the Program input box, then press Run again." : "Check what it was given to read.");
  }
  if (!output.includes("java.util.NoSuchElementException") || !output.includes("java.util.Scanner")) return null;
  if (!input.box) {
    return "The program asked for input, but there was none to read. Code here runs without a keyboard, so give it fixed values instead.";
  }
  return input.given
    ? "The program asked for more input than you gave it. Add another line to the Program input box, then press Run again."
    : "Your code may be fine: the program asked for input, but none was given. Type what the rider would type in the Program input box, one answer per line, then press Run again.";
}
