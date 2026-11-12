import { h, icon } from "./dom.js";
import { api } from "./api.js";

const TYPE_LABELS = {
  "mcq": "Pick one",
  "fill-blank": "Fill in the blank",
  "predict-output": "Predict the output",
  "order": "Put in order",
  "spot-the-bug": "Spot the bug",
};

export async function renderQuiz(container, topicId, chat, showSection, onProgress) {
  container.replaceChildren(h("p", { class: "loading" }, "Getting your check ready…"));
  const attempt = await api.quiz(topicId);
  const readers = new Map();

  const form = h("form", { class: "quiz", onsubmit: submit },
    h("p", { class: "quiz-intro" },
      `${attempt.questions.length} questions. You need ${attempt.passPercent}% to pass. Each try shows a fresh mix, so retrying is fine.`),
    attempt.questions.map((q, i) => {
      const { node, read } = QUESTION[q.type](q, i);
      readers.set(q.id, read);
      return h("fieldset", { class: "question", "data-qid": q.id },
        h("legend", {}, `${i + 1}. ${q.type === "fill-blank" ? "Fill in the missing part of this line." : q.prompt} `,
          h("span", { class: "question-type" }, TYPE_LABELS[q.type])), node);
    }),
    h("button", { type: "submit", class: "button button-primary" }, icon("send"), "Check my answers"));
  container.replaceChildren(form);

  async function submit(event) {
    event.preventDefault();
    const answers = {};
    for (const [id, read] of readers) {
      const value = read();
      if (value !== undefined) answers[id] = value;
    }
    const button = form.querySelector("button[type=submit]");
    button.disabled = true;
    try {
      const { result, progress } = await api.gradeQuiz(topicId, attempt.seed, answers);
      showResult(result, progress);
      onProgress();
    } catch (error) {
      chat.say(`I couldn't grade that: ${error.message}`, { mood: "encouraging" });
      button.disabled = false;
    }
  }

  function showResult(result, progress) {
    form.querySelectorAll("input, textarea, button").forEach((el) => { el.disabled = true; });
    for (const graded of result.questions) {
      const fieldset = form.querySelector(`[data-qid="${CSS.escape(graded.id)}"]`);
      fieldset.append(h("div", { class: `verdict ${graded.correct ? "verdict-right" : "verdict-wrong"}` },
        h("strong", {}, graded.correct ? "Correct." : "Not quite."),
        graded.explain,
        !graded.correct && h("pre", {}, h("code", {}, `Answer: ${graded.expected}`)),
        !graded.correct && graded.reteach && h("p", {},
          h("a", { href: "#", onclick: (e) => { e.preventDefault(); showSection(graded.reteach); } },
            "Read that part of the KB page again"))));
    }
    const banner = h("div", { class: `quiz-result ${result.passed ? "quiz-result-pass" : "quiz-result-fail"}`, role: "status" },
      h("span", { class: "quiz-score" }, `${result.scorePercent}%`),
      h("span", {}, result.passed
        ? (progress.testsPassed ? "Check passed." : "Check passed. Now go fix the code.")
        : `You need ${attempt.passPercent}% to pass. Your best so far is ${progress.quizBest}%.`),
      h("button", { type: "button", class: "button", onclick: () => renderQuiz(container, topicId, chat, showSection, onProgress) },
        icon("rotate-ccw"), result.passed ? "Take another set" : "Try a new set"));
    form.prepend(banner);
    banner.scrollIntoView({ behavior: "smooth", block: "start" });
    if (result.passed) {
      if (!progress.testsPassed) chat.say(`${result.scorePercent}%! You know this. Open the Fix tab and get the checks green.`, { mood: "cheering" });
    } else {
      chat.say([`${result.scorePercent}% this time. Close!`,
        `Have another look at: ${result.weakConcepts.join(", ")}. Then try a new set.`], { mood: "encouraging" });
    }
  }
}

const QUESTION = {
  "mcq": (q, i) => {
    const name = `q${i}`;
    const looksLikeCode = q.choices.some((choice) => /[;(){}"=]|^\w+\.\w+$/.test(choice));
    const node = h("div", { role: "radiogroup" }, q.choices.map((choice, c) =>
      h("label", { class: "choice" }, h("input", { type: "radio", name, value: c }),
        looksLikeCode ? h("code", {}, choice) : h("span", {}, choice))));
    return { node, read: () => {
      const picked = node.querySelector("input:checked");
      return picked ? Number(picked.value) : undefined;
    } };
  },
  "fill-blank": (q, i) => {
    const [before, after] = q.prompt.split("___");
    const input = h("input", { class: "blank-input", type: "text", name: `q${i}`, "aria-label": "Your answer for the blank", autocomplete: "off", spellcheck: "false" });
    return { node: h("p", {}, h("code", {}, before), input, h("code", {}, after ?? "")), read: () => input.value };
  },
  "predict-output": (q, i) => {
    const area = h("textarea", { class: "output-input", name: `q${i}`, "aria-label": "What the program prints", spellcheck: "false" });
    return { node: h("div", {}, h("pre", {}, h("code", {}, q.code)), area), read: () => area.value };
  },
  "order": (q) => {
    const list = h("ol", { class: "order-list" });
    let dragged;
    const move = (item, delta) => {
      const items = [...list.children];
      const target = items.indexOf(item) + delta;
      if (target < 0 || target >= items.length) return;
      if (delta < 0) list.insertBefore(item, items[target]);
      else list.insertBefore(item, items[target].nextSibling);
      item.querySelector(delta < 0 ? ".up" : ".down").focus();
    };
    q.items.forEach((text, index) => {
      const item = h("li", { class: "order-item", draggable: "true", "data-index": index },
        h("code", {}, text),
        h("button", { type: "button", class: "icon-button up", "aria-label": `Move up: ${text}`, onclick: () => move(item, -1) }, icon("arrow-up")),
        h("button", { type: "button", class: "icon-button down", "aria-label": `Move down: ${text}`, onclick: () => move(item, 1) }, icon("arrow-down")));
      item.addEventListener("dragstart", () => { dragged = item; item.classList.add("dragging"); });
      item.addEventListener("dragend", () => item.classList.remove("dragging"));
      item.addEventListener("dragover", (e) => {
        e.preventDefault();
        if (!dragged || dragged === item) return;
        const box = item.getBoundingClientRect();
        list.insertBefore(dragged, e.clientY < box.top + box.height / 2 ? item : item.nextSibling);
      });
      list.append(item);
    });
    return { node: list, read: () => [...list.children].map((li) => Number(li.dataset.index)) };
  },
  "spot-the-bug": (q, i) => {
    const name = `q${i}`;
    const node = h("div", { class: "line-pick", role: "radiogroup" }, q.code.replace(/\n$/, "").split("\n").map((line, n) =>
      h("label", {}, h("span", { class: "line-no" }, String(n + 1)), h("input", { type: "radio", name, value: n + 1, "aria-label": `Line ${n + 1}` }), h("code", {}, line))));
    return { node, read: () => {
      const picked = node.querySelector("input:checked");
      return picked ? Number(picked.value) : undefined;
    } };
  },
};
