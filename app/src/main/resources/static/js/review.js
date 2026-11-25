import { h, icon } from "./dom.js";
import { api } from "./api.js";
import { byte, HELPER } from "./byte.js";
import { questionFieldset, readAnswers } from "./quiz.js";
import { refreshRewards } from "./rewards.js";

const VERDICTS = {
  GOOD: ["All three right. This one is sticking. It comes back in a while.", "cheering"],
  HARD: ["Two of three. It comes back sooner so it sticks.", "encouraging"],
  AGAIN: ["This one slipped a bit. It's on your weak list and comes back tomorrow.", "encouraging"],
};

export async function renderReview(view, catalog, id) {
  const topic = catalog.topics.find((t) => t.id === id);
  const attempt = await api.review(id);
  const readers = new Map();
  const form = h("form", { class: "quiz", onsubmit: submit },
    attempt.questions.map((q, i) => questionFieldset(q, i, readers)),
    h("button", { type: "submit", class: "button button-primary" }, icon("send"), "Check my answers"));

  view.replaceChildren(h("div", { class: "review-page" },
    h("p", { class: "key" }, `Review · ${topic?.ticket.key ?? ""}`),
    h("h1", {}, topic?.ticket.title ?? id),
    h("div", { class: "board-greeting" }, byte("idle"),
      h("p", {}, h("strong", {}, `${HELPER.name}: `), "Three quick questions from a ticket you closed. No looking back at the KB page first!")),
    form));

  async function submit(event) {
    event.preventDefault();
    form.querySelector("button[type=submit]").disabled = true;
    const { result, grade } = await api.gradeReview(id, attempt.seed, readAnswers(readers));
    form.querySelectorAll("input, textarea, button").forEach((el) => { el.disabled = true; });
    for (const graded of result.questions) {
      form.querySelector(`[data-qid="${CSS.escape(graded.id)}"]`).append(
        h("div", { class: `verdict ${graded.correct ? "verdict-right" : "verdict-wrong"}` },
          h("strong", {}, graded.correct ? "Correct." : "Not quite."), graded.explain,
          !graded.correct && h("pre", {}, h("code", {}, `Answer: ${graded.expected}`))));
    }
    const [text, mood] = VERDICTS[grade];
    form.prepend(h("div", { class: `quiz-result ${grade === "AGAIN" ? "quiz-result-fail" : "quiz-result-pass"}`, role: "status" },
      byte(mood, ""), h("span", {}, text),
      h("a", { class: "button", href: "/career" }, "Back to your career page")));
    form.scrollIntoView({ behavior: "smooth", block: "start" });
    refreshRewards();
  }
  return () => {};
}
