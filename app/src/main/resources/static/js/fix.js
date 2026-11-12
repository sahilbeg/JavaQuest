import { h, icon } from "./dom.js";
import { api } from "./api.js";
import { bus } from "./events.js";
import { loadMonaco, editorTheme, EDITOR_OPTIONS } from "./monaco.js";
import { describeProgram } from "./kb.js";

export async function renderFix(container, topic, chat, onResult) {
  const id = topic.summary.id;
  const path = topic.sourceFiles[0];
  if (!path) {
    container.replaceChildren(h("p", { class: "empty" }, "This ticket has no code to fix. Pass the onboarding check to close it."));
    return () => {};
  }

  const fileLabel = h("span", { class: "editor-file" }, path);
  const host = h("div", { class: "editor-host" });
  const notice = h("div", { class: "notice", hidden: true, role: "status" });
  const checks = h("section", { class: "checks", "aria-labelledby": "checks-title" });
  const saveButton = h("button", { type: "button", class: "button button-quiet", onclick: () => save() }, icon("save"), "Save");
  const runButton = topic.mainClass && h("button", { type: "button", class: "button", onclick: run }, icon("play"), "Run");
  const submitButton = h("button", { type: "button", class: "button button-primary", onclick: submit }, icon("send"), "Run checks");

  container.replaceChildren(
    h("p", { class: "fix-intro" }, "Edit the code here or in your IDE at ",
      h("code", {}, `practice/${id}/src/${path}`),
      ". Saving in your IDE runs the checks for you."),
    h("div", { class: "editor-frame" },
      h("div", { class: "editor-bar" }, fileLabel, h("span", { class: "spacer" }), saveButton, runButton, submitButton),
      notice, host),
    checks);

  renderChecks(checks, topic.summary.latest);

  const monaco = await loadMonaco();
  const { content } = await api.readSource(id, path);
  const editor = monaco.editor.create(host, { ...EDITOR_OPTIONS, value: content, theme: editorTheme() });
  let savedValue = content;
  const markDirty = () => { fileLabel.dataset.dirty = String(editor.getValue() !== savedValue); };
  editor.onDidChangeModelContent(markDirty);
  editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => save());

  async function save() {
    if (editor.getValue() === savedValue) return;
    await api.writeSource(id, path, editor.getValue());
    savedValue = editor.getValue();
    markDirty();
  }

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
      const { result, explanations } = await api.runMain(id);
      checks.replaceChildren(h("h2", { id: "checks-title", class: "console-label" }, "Program output"),
        h("pre", { class: "console-out" }, describeProgram(result, explanations)));
    });
  }

  async function submit() {
    await busy(submitButton, async () => {
      await save();
      await api.submit(id);
    });
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
    notice.replaceChildren("This file changed in your IDE, and you have unsaved edits here.",
      h("button", { type: "button", class: "button", onclick: () => {
        savedValue = fresh.content;
        editor.setValue(fresh.content);
        notice.hidden = true;
        markDirty();
      } }, "Load the IDE version"),
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
    onResult(e.detail);
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
    container.replaceChildren(head(null), h("p", { class: "checks-empty" }, "No checks have run yet. Press Run checks, or save the file in your IDE."));
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
        h("span", { class: "check-raw" }, `Java says: ${e.message.split("\n")[0]}`)))
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
    rows.length && h("ul", { class: "check-list" }, rows),
    extra && h("p", { class: "checks-empty" }, extra),
    r.output && h("p", { class: "console-label" }, "Your program printed"),
    r.output && h("pre", { class: "console-out" }, r.output)].filter(Boolean));
}

function narrate(chat, practice) {
  const r = practice.result;
  const where = practice.source === "IDE" ? "I saw your save in the IDE. " : "";
  if (r.outcome === "PASSED") {
    chat.say(`${where}All checks pass. Ticket closed, nice work!`, { mood: "cheering" });
  } else if (r.outcome === "COMPILE_ERROR") {
    chat.say(`${where}Java can't compile this yet. Start with the first error, line ${r.compileErrors[0]?.line}.`, { mood: "encouraging" });
  } else if (r.outcome === "FAILED") {
    const passed = r.tests.filter((t) => t.passed).length;
    chat.say(`${where}${passed} of ${r.tests.length} checks pass. Read the first red one: it says what it expected.`, { mood: "encouraging" });
  } else {
    chat.say(`${where}The checks didn't finish. See the note under Checks.`, { mood: "encouraging" });
  }
}
