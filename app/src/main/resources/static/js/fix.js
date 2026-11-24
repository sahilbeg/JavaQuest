import { h, icon } from "./dom.js";
import { api } from "./api.js";
import { bus } from "./events.js";
import { loadMonaco, editorTheme, colorize, languageFor, EDITOR_OPTIONS } from "./monaco.js";
import { describeProgram } from "./kb.js";
import { vscodeGuide } from "./vscode.js";

export async function renderFix(container, topic, chat, onResult) {
  const id = topic.summary.id;
  let path = topic.sourceFiles[0];
  if (!path) {
    container.replaceChildren(h("p", { class: "empty" }, "This ticket has no code to fix. Pass the onboarding check to close it."));
    return () => {};
  }

  const fileLabel = h("span", { class: "editor-file" }, path);
  // Web tickets have several files (HTML, CSS, JavaScript): one tab each. A Java ticket keeps its single file label.
  const tabs = topic.sourceFiles.length > 1 && h("div", { class: "editor-tabs", role: "tablist", "aria-label": "Files" },
    topic.sourceFiles.map((file) => h("button", { type: "button", role: "tab", class: "editor-tab", "data-file": file,
      "aria-selected": String(file === path), onclick: () => openFile(file) }, file.split("/").pop())));
  const previewFrame = topic.preview && h("iframe", { class: "preview-frame", title: "Preview of your page",
    sandbox: "allow-scripts allow-forms allow-modals" });
  const previewPanel = topic.preview && h("section", { class: "preview-panel", "aria-labelledby": "preview-title" },
    h("div", { class: "preview-bar" },
      h("h2", { id: "preview-title", class: "console-label" }, "Preview"),
      h("span", { class: "spacer" }),
      h("a", { class: "button button-quiet", href: `/preview/${id}/`, target: "_blank", rel: "noopener" }, icon("external-link"), "Open in a new tab"),
      h("button", { type: "button", class: "button button-quiet", onclick: () => refreshPreview() }, icon("rotate-ccw"), "Refresh")),
    previewFrame);
  const host = h("div", { class: "editor-host" });
  const notice = h("div", { class: "notice", hidden: true, role: "status" });
  const checks = h("section", { class: "checks", "aria-labelledby": "checks-title" });
  const saveButton = h("button", { type: "button", class: "button button-quiet", onclick: () => save() }, icon("save"), "Save");
  const runButton = topic.mainClass && h("button", { type: "button", class: "button", onclick: run }, icon("play"), "Run");
  const submitButton = h("button", { type: "button", class: "button button-primary", id: "run-checks", onclick: submit }, icon("send"), "Run checks");
  const solutionButton = h("button", { type: "button", class: "button button-quiet", onclick: showSolution }, icon("lightbulb"), "Show solution");
  const resetButton = h("button", { type: "button", class: "button button-quiet", onclick: confirmReset }, icon("rotate-ccw"), "Reset");
  const solutionPanel = h("section", { class: "solution", hidden: true, "aria-labelledby": "solution-title" });
  const inputBox = h("textarea", { id: "program-input", class: "program-input", rows: 3, spellcheck: "false",
    placeholder: "12\nOld Mill", "aria-describedby": "program-input-help" });
  const inputPanel = runButton && h("div", { class: "program-input-panel", hidden: true },
    h("label", { for: "program-input", class: "console-label" }, "Program input"),
    h("p", { id: "program-input-help", class: "program-input-help" },
      "Your program reads from the keyboard. Type what the rider would type, one answer per line. Run sends it to the program."),
    inputBox);

  const toChange = (topic.codeGuide?.files ?? []).filter((f) => f.toChange);
  const name = path.split("/").pop();
  container.replaceChildren(...[
    vscodeGuide(topic),
    h("p", { class: "fix-intro" }, "Or type right here instead. ",
      toChange.length > 1 || (toChange.length === 1 && toChange[0].path !== path)
        ? ["This editor only shows ", h("code", {}, name), ", and this ticket needs changes in ",
            toChange.map((f, i) => [i ? (i === toChange.length - 1 ? " and " : ", ") : "", h("code", {}, f.path.split("/").pop())]),
            ", so VS Code is the easier place for it."]
        : "Press Run checks when you're done. Saving in VS Code runs the same checks."),
    h("div", { class: "editor-frame" },
      h("div", { class: "editor-bar" }, tabs || fileLabel, h("span", { class: "spacer" }), saveButton, runButton, submitButton),
      notice, host),
    previewPanel,
    h("div", { class: "editor-foot" },
      h("p", { class: "editor-keys" }, "Keyboard: Tab indents code inside the editor. Press ",
        h("kbd", {}, navigator.platform.startsWith("Mac") ? "Ctrl Shift M" : "Ctrl M"),
        " to make Tab move focus out of the editor instead."),
      h("span", { class: "spacer" }), solutionButton, resetButton),
    inputPanel,
    checks,
    solutionPanel].filter(Boolean));

  renderChecks(checks, topic.summary.latest);

  const monaco = await loadMonaco();
  const { content } = await api.readSource(id, path);
  const language = languageFor(path);
  const editor = monaco.editor.create(host, { ...EDITOR_OPTIONS, language, tabSize: language === "java" ? 4 : 2, value: content, theme: editorTheme() });
  let savedValue = content;
  const markDirty = () => { fileLabel.dataset.dirty = String(editor.getValue() !== savedValue); };
  // Show the input box only for programs that read the keyboard, and never hide what the learner typed.
  const showInput = () => {
    if (inputPanel) inputPanel.hidden = !(/System\.in\b|readFileSync\(0|process\.stdin/.test(editor.getValue()) || inputBox.value);
  };
  showInput();
  editor.onDidChangeModelContent(() => { markDirty(); showInput(); });
  editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => save());

  async function save() {
    if (editor.getValue() === savedValue) return;
    await api.writeSource(id, path, editor.getValue());
    savedValue = editor.getValue();
    markDirty();
    refreshPreview();
  }

  /** Switches the editor to another file of this ticket, saving the current one first. */
  async function openFile(file) {
    if (file === path) return;
    await save();
    const fresh = await api.readSource(id, file);
    path = file;
    fileLabel.textContent = file;
    savedValue = fresh.content;
    monaco.editor.setModelLanguage(editor.getModel(), languageFor(file));
    editor.updateOptions({ tabSize: languageFor(file) === "java" ? 4 : 2 });
    editor.setValue(fresh.content);
    markDirty();
    for (const tab of tabs ? tabs.querySelectorAll(".editor-tab") : []) tab.setAttribute("aria-selected", String(tab.dataset.file === file));
    editor.focus();
  }

  function refreshPreview() {
    if (previewFrame) previewFrame.src = `/preview/${id}/?t=${Date.now()}`;
  }
  refreshPreview();

  async function busy(button, task) {
    button.setAttribute("aria-busy", "true");
    button.disabled = true;
    try {
      await task();
    } catch (error) {
      chat.say(`Something went wrong: ${error.message}`, { mood: "encouraging" });
    } finally {
      button.removeAttribute("aria-busy");
      button.disabled = false;
    }
  }

  async function run() {
    await busy(runButton, async () => {
      await save();
      const input = inputPanel && !inputPanel.hidden ? inputBox.value : "";
      const typed = input && !input.endsWith("\n") ? input + "\n" : input;
      const { result, explanations } = await api.runMain(id, typed);
      checks.replaceChildren(h("h2", { id: "checks-title", class: "console-label" }, "Program output"),
        h("pre", { class: "console-out" },
          describeProgram(result, explanations, { box: Boolean(inputPanel && !inputPanel.hidden), given: Boolean(input.trim()) })));
    });
  }

  async function submit() {
    await busy(submitButton, async () => {
      await save();
      await api.submit(id);
    });
  }

  async function showSolution() {
    try {
      const { files, progress } = await api.solution(id);
      solutionPanel.replaceChildren(
        h("h2", { id: "solution-title", class: "console-label" }, "Tsunade's solution"),
        ...files.map((f) => h("div", {}, h("p", { class: "editor-file" }, f.path), h("pre", { class: "console-out solution-code" }, h("code", { "data-path": f.path }, f.content)))));
      solutionPanel.hidden = false;
      solutionPanel.scrollIntoView({ behavior: "smooth", block: "start" });
      for (const code of solutionPanel.querySelectorAll("code")) {
        const html = await colorize(monaco, code.textContent, languageFor(code.dataset.path));
        if (html) code.innerHTML = html;
      }
      chat.say(progress.testsPassed
        ? "Here's how Tsunade wrote it. Compare it with yours: there's usually more than one good answer."
        : "Here's the solution. Read it line by line, then type it yourself instead of pasting. Typing it is how it sticks.",
        { mood: "idle" });
    } catch (error) {
      chat.say(error.message, { mood: "encouraging" });
    }
  }

  function confirmReset() {
    notice.replaceChildren("Put the starting code back? Your changes to this file will be lost.",
      h("button", { type: "button", class: "button", onclick: async () => {
        notice.hidden = true;
        await api.reset(id);
        const fresh = await api.readSource(id, path);
        savedValue = fresh.content;
        editor.setValue(fresh.content);
        markDirty();
        chat.say("Fresh start. The starting code is back.", { mood: "idle" });
      } }, "Reset the code"),
      h("button", { type: "button", class: "button button-quiet", onclick: () => { notice.hidden = true; } }, "Cancel"));
    notice.hidden = false;
  }

  async function reloadFromDisk() {
    const fresh = await api.readSource(id, path);
    if (fresh.content === savedValue) return;
    if (editor.getValue() === savedValue) {
      savedValue = fresh.content;
      editor.setValue(fresh.content);
      markDirty();
      return;
    }
    notice.replaceChildren("This file changed in VS Code, and you have unsaved edits here.",
      h("button", { type: "button", class: "button", onclick: () => {
        savedValue = fresh.content;
        editor.setValue(fresh.content);
        notice.hidden = true;
        markDirty();
      } }, "Load the VS Code version"),
      h("button", { type: "button", class: "button button-quiet", onclick: () => { notice.hidden = true; } }, "Keep mine"));
    notice.hidden = false;
  }

  const onStarted = (e) => {
    if (e.detail.topicId !== id) return;
    checks.replaceChildren(h("div", { class: "checks-head" },
      h("h2", { id: "checks-title" }, "Checks"), h("span", { class: "chip chip-progress" }, "Running")));
  };
  const onFinished = async (e) => {
    if (e.detail.topicId !== id) return;
    renderChecks(checks, e.detail);
    narrate(chat, e.detail);
    await onResult();
    if (e.detail.source === "IDE") await reloadFromDisk();
  };
  bus.addEventListener("run-started", onStarted);
  bus.addEventListener("run-finished", onFinished);
  return () => {
    bus.removeEventListener("run-started", onStarted);
    bus.removeEventListener("run-finished", onFinished);
    editor.dispose();
  };
}

function renderChecks(container, practice) {
  const head = (chip) => h("div", { class: "checks-head" },
    h("h2", { id: "checks-title" }, "Checks"), chip,
    practice && h("span", { class: "source" }, practice.source === "IDE" ? "from your IDE save" : "from this page"));
  if (!practice) {
    container.replaceChildren(head(null), h("p", { class: "checks-empty" }, "No checks have run yet. Save your file in VS Code, or press Run checks."));
    return;
  }
  const r = practice.result;
  const passed = r.tests.filter((t) => t.passed).length;
  const chip = {
    PASSED: h("span", { class: "chip chip-done" }, `All ${r.tests.length} checks pass`),
    FAILED: h("span", { class: "chip chip-failing" }, `${passed} of ${r.tests.length} checks pass`),
    COMPILE_ERROR: h("span", { class: "chip chip-failing" }, "Doesn't compile"),
    TIMED_OUT: h("span", { class: "chip chip-failing" }, "Took too long"),
    CRASHED: h("span", { class: "chip chip-failing" }, "Crashed"),
  }[r.outcome];

  const rows = r.outcome === "COMPILE_ERROR"
    ? r.compileErrors.map((e, i) => h("li", { class: "check check-fail" }, icon("circle-x", "Error"),
        h("span", { class: "check-name" }, `${e.file}, line ${e.line}`),
        practice.explanations[i] && h("span", { class: "check-plain" }, practice.explanations[i]),
        h("span", { class: "check-raw" }, `${/\.java$/.test(e.file ?? "") ? "Java" : "JavaScript"} says: ${e.message.split("\n")[0]}`)))
    : r.tests.map((t) => h("li", { class: `check ${t.passed ? "check-pass" : "check-fail"}` },
        icon(t.passed ? "circle-check" : "circle-x", t.passed ? "Passed" : "Failed"),
        h("span", { class: "check-name" }, t.name),
        !t.passed && t.message && h("span", { class: "check-message" }, t.message)));

  const extra = {
    TIMED_OUT: "The checks ran for more than 10 seconds and were stopped. Look for a loop that never ends.",
    CRASHED: r.output.includes("OutOfMemoryError")
      ? "Your code used up all its memory. Look for something that keeps growing, like a list in an endless loop."
      : "The checks stopped before they finished. Did the code call System.exit?",
  }[r.outcome];

  container.replaceChildren(...[head(chip),
    tierRow(r.tests),
    rows.length && h("ul", { class: "check-list" }, rows),
    extra && h("p", { class: "checks-empty" }, extra),
    r.output && h("p", { class: "console-label" }, "Your program printed"),
    r.output && h("pre", { class: "console-out" }, r.output)].filter(Boolean));
}

const TIERS = ["Bronze", "Silver", "Gold"];

/** Java: KioskBronzeTest. Web: test/CartBronze.test.jsx. */
const ownsTier = (className, tier) => className.endsWith(`${tier}Test`) || new RegExp(`${tier}\\.test\\.jsx?$`).test(className);

function tierRow(tests) {
  const tiers = TIERS.map((tier) => {
    const own = tests.filter((t) => ownsTier(t.className, tier));
    return { tier, own, passed: own.length > 0 && own.every((t) => t.passed) };
  }).filter((t) => t.own.length > 0);
  if (!tiers.length) return null;
  return h("ul", { class: "tier-row", "aria-label": "Incident tiers" }, tiers.map((t) =>
    h("li", { class: `tier tier-${t.tier.toLowerCase()} ${t.passed ? "is-passed" : ""}` },
      icon(t.passed ? "circle-check" : "circle-x"), `${t.tier}: ${t.own.filter((x) => x.passed).length} of ${t.own.length}`)));
}

function narrate(chat, practice) {
  const r = practice.result;
  const where = practice.source === "IDE" ? "I saw your save in VS Code. " : "";
  if (r.outcome === "PASSED") {
    chat.say(`${where}All checks pass!`, { mood: "cheering" });
  } else if (r.outcome === "COMPILE_ERROR") {
    chat.say(`${where}This code can't run yet. Start with the first error, line ${r.compileErrors[0]?.line}.`, { mood: "encouraging" });
  } else if (r.outcome === "FAILED" && r.tests.some((t) => ownsTier(t.className, "Bronze"))) {
    const open = TIERS.filter((tier) => r.tests.some((t) => t.className.endsWith(`${tier}Test`) && !t.passed));
    chat.say(open.includes("Bronze")
      ? `${where}The bronze checks still fail, so the incident is still open. Start with those.`
      : `${where}Bronze passes, so the incident is resolved. ${open.join(" and ")} still ${open.length === 1 ? "has" : "have"} a failing check if you want the bonus.`,
      { mood: open.includes("Bronze") ? "encouraging" : "cheering" });
  } else if (r.outcome === "FAILED") {
    const passed = r.tests.filter((t) => t.passed).length;
    chat.say(`${where}${passed} of ${r.tests.length} checks pass. Read the first red one: it says what it expected.`, { mood: "encouraging" });
  } else {
    chat.say(`${where}The checks didn't finish. See the note under Checks.`, { mood: "encouraging" });
  }
}
