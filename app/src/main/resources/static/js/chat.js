import { h, icon } from "./dom.js";
import { byte, HELPER } from "./byte.js";
import { api } from "./api.js";

/**
 * Gojo, the team's helper: a floating button that opens a chat drawer with messages and hints.
 * Returns { fab, drawer, say, open, close }. A message that arrives while the drawer is closed
 * puts a dot on the button.
 */
export function createHelper(topic) {
  const log = h("div", { class: "chat-log", role: "log", "aria-live": "polite", "aria-label": `Messages from ${HELPER.name}` });
  let used = 0;

  const hintButton = h("button", { type: "button", class: "button", onclick: askHint });
  const updateHintButton = () => {
    hintButton.replaceChildren(icon("lightbulb"),
      used < topic.hintCount ? `Get hint ${used + 1} of ${topic.hintCount}` : "No hints left");
    hintButton.disabled = used >= topic.hintCount;
  };

  const fab = h("button", { type: "button", class: "helper-fab", id: "helper-fab", "aria-label": `Chat with ${HELPER.name}`,
    "aria-expanded": "false", "aria-controls": "helper", onclick: () => (drawer.classList.contains("is-open") ? close() : open()) },
    byte("idle", "", { size: "md" }));
  const closeButton = h("button", { type: "button", class: "icon-button", "aria-label": "Close the chat", onclick: () => close() }, icon("x"));
  const drawer = h("aside", { class: "helper", id: "helper", "aria-label": `Chat with ${HELPER.name}`, tabindex: "-1" },
    h("div", { class: "chat-head" }, byte("idle", "", { size: "sm" }),
      h("div", {}, h("h2", {}, HELPER.fullName), h("p", {}, "Your helper. Ask for a hint any time.")), closeButton),
    log,
    h("div", { class: "chat-actions" }, hintButton));

  function open() {
    drawer.classList.add("is-open");
    fab.setAttribute("aria-expanded", "true");
    fab.removeAttribute("data-unread");
    drawer.focus({ preventScroll: true });
  }

  function close() {
    drawer.classList.remove("is-open");
    fab.setAttribute("aria-expanded", "false");
    fab.focus({ preventScroll: true });
  }

  drawer.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      e.stopPropagation();
      close();
    }
  });

  function say(text, { mood = "idle", kind = "", fresh = true, quiet = false } = {}) {
    const paragraphs = Array.isArray(text) ? text : [text];
    const message = h("div", { class: `message ${kind ? `message-${kind}` : ""}`, "data-new": fresh || null },
      byte(mood, ""),
      h("div", { class: "message-who" }, h("strong", {}, HELPER.name), kind === "hint" ? "hint" : ""),
      h("div", { class: "message-text" }, paragraphs.map((p) => h("p", {}, p))));
    log.append(message);
    log.scrollTop = log.scrollHeight;
    fab.querySelector(".byte").dataset.mood = mood;
    if (!quiet && !drawer.classList.contains("is-open")) fab.setAttribute("data-unread", "");
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

  updateHintButton();
  api.hints(topic.summary.id).then(({ opened }) => {
    used = opened.length;
    opened.forEach((hint) => say(hint, { kind: "hint", mood: "thinking", fresh: false, quiet: true }));
    updateHintButton();
  }).catch(() => {});

  return { fab, drawer, say, open, close };
}
