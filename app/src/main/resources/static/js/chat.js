import { h, icon } from "./dom.js";
import { byte } from "./byte.js";
import { api } from "./api.js";


export function createChat(container, topic) {
  const log = h("div", { class: "chat-log", role: "log", "aria-live": "polite", "aria-label": "Messages from Byte" });
  let used = 0;

  const hintButton = h("button", { type: "button", class: "button", onclick: askHint });
  const updateHintButton = () => {
    hintButton.replaceChildren(icon("lightbulb"),
      used < topic.hintCount ? `Get hint ${used + 1} of ${topic.hintCount}` : "No hints left");
    hintButton.disabled = used >= topic.hintCount;
  };

  function say(text, { mood = "idle", kind = "", fresh = true } = {}) {
    const paragraphs = Array.isArray(text) ? text : [text];
    const message = h("div", { class: `message ${kind ? `message-${kind}` : ""}`, "data-new": fresh || null },
      byte(mood, ""),
      h("div", { class: "message-who" }, h("strong", {}, "Byte"), kind === "hint" ? "hint" : "team helper"),
      h("div", { class: "message-text" }, paragraphs.map((p) => h("p", {}, p))));
    log.append(message);
    log.scrollTop = log.scrollHeight;
  }

  async function askHint() {
    hintButton.disabled = true;
    try {
      const { hint, progress } = await api.nextHint(topic.summary.id);
      used = progress.hintsUsed;
      say(hint, { kind: "hint", mood: "thinking" });
    } catch (error) {
      say(error.message, { mood: "encouraging" });
    }
    updateHintButton();
  }

  container.replaceChildren(
    h("div", { class: "chat-head" }, byte("idle", ""),
      h("div", {}, h("h2", {}, "Byte"), h("p", {}, "Team helper in #onboarding"))),
    log,
    h("div", { class: "chat-actions" }, hintButton));
  updateHintButton();

  api.hints(topic.summary.id).then(({ opened }) => {
    used = opened.length;
    opened.forEach((hint) => say(hint, { kind: "hint", mood: "thinking", fresh: false }));
    updateHintButton();
  }).catch(() => {});

  return { say };
}
