// First-visit walkthroughs. Gojo spotlights one thing at a time and explains it in a bubble.
// Each page's tour plays once (remembered in this browser); the ? button in the top bar replays it.

import { h, icon } from "./dom.js";
import { byte } from "./byte.js";

const TOURS = {
  journey: [
    { title: "Welcome to JavaQuest", text: "You're the new engineering intern at Spoke, a bike-sharing startup. Every lesson is a real ticket from the team. Here's a quick tour." },
    { target: "#up-next", title: "Up next", text: "The one ticket to work on now. Tickets open one at a time, so each builds on the last." },
    { target: "#current-stop", title: "Course outline", text: "Your current module, with its tickets in order. Finish them all to unlock the next module." },
    { target: "#career-chip", title: "Level and streak", text: "Tickets earn XP and move you up the career ladder. A little every day keeps your streak going." },
    { target: "#resume-link", title: "Continue", text: "Back to the exact card you left, from any page. It survives reloads and rebuilds.", optional: true },
    { target: '[data-nav="notes"]', title: "Notes", text: "Every ticket you finish gets a short page of notes: key points, syntax and common mistakes. Come back to them any time." },
    { target: "#daily-limit", title: "Daily limit", text: "Switch it on to learn a few tickets a day and the rest tomorrow. It stays up here on every page, so you can change it any time." },
    { target: "#tour-button", title: "Tour", text: "Replay this walkthrough on any page from here." },
  ],
  lesson: [
    { target: "#lesson-progress", title: "Lesson progress", text: "Each ticket is a short sequence of cards. Jump back to any card you've already seen." },
    { target: "#lesson-card", title: "One idea per card", text: "Read, then press Next, or use the arrow keys." },
    { target: ".anim", title: "Animations", text: "Watch code run step by step, or move through it at your own pace.", optional: true },
    { target: "#helper-fab", title: "Hints", text: "Gojo, the team's helper, has hints for this ticket. More open as you try." },
    { target: "#player-next", title: "Continue", text: "Move on whenever you're ready." },
  ],
  check: [
    { target: ".quiz", title: "The onboarding check", text: "A short mixed set of questions. 80% passes, and every retry gives you a fresh mix." },
    { target: ".question", title: "Read each question", text: "If you miss one, I'll link you straight back to the card that explains it." },
  ],
  code: [
    { target: ".vscode-guide > summary", title: "Code in VS Code", text: "Kakashi shows where this ticket's files are, step by step. Saving a file in VS Code submits it.", optional: true },
    { target: ".editor-frame", title: "Or fix it here", text: "Small change? Edit here and press Run checks. It runs the same checks as a save in VS Code." },
    { target: "#run-checks", title: "Run the checks", text: "Real JUnit tests check your code. Make every check green to finish the ticket." },
    { target: ".checks", title: "Read the red ones", text: "Each failing check says what it expected. Start with the first red one.", mood: "encouraging" },
  ],
  notes: [
    { target: "#notes-list", title: "Your notes", text: "One page for every ticket you've finished, taken from its lesson. Open one when you need a quick reminder." },
    { target: "#notes-search", title: "Find a topic", text: "Type part of a title or a ticket key to narrow the list." },
  ],
  career: [
    { target: ".career-hero", title: "Your career", text: "Your job title, level and XP. Promotions come every five levels." },
    { target: ".streak-card", title: "Your streak", text: "Earn a little XP every day to keep it going. Freezes protect a missed day." },
    { target: ".kudos-grid", title: "Kudos", text: "Teammates send kudos for great work. Locked ones say how to earn them.", mood: "cheering" },
  ],
};

const seenKey = (name) => `jq-tour:${name}`;

function seen(name) {
  try { return localStorage.getItem(seenKey(name)) === "done"; } catch { return false; }
}

function markSeen(name) {
  try { localStorage.setItem(seenKey(name), "done"); } catch { /* storage blocked: the tour shows again next time */ }
}

let active;
let pageTour = "journey";

/** The tour the ? button plays on the current page. */
export function setPageTour(name) {
  pageTour = name;
}

export function replayPageTour() {
  startTour(pageTour);
}

/** Plays a tour the first time a page is visited. Waits a moment so the page can finish rendering. */
export function startTourOnce(name, delay = 500) {
  setPageTour(name);
  if (seen(name)) return;
  setTimeout(() => {
    if (!seen(name) && !active) startTour(name);
  }, delay);
}

const visible = (el) => el && el.getClientRects().length > 0;

export function startTour(name) {
  active?.close();
  const steps = (TOURS[name] ?? []).filter((s) => !s.target || visible(document.querySelector(s.target)) || !s.optional);
  const usable = steps.filter((s) => !s.target || visible(document.querySelector(s.target)));
  if (!usable.length) return;
  const returnFocus = document.activeElement;
  const spot = h("div", { class: "tour-spot" });
  const bubble = h("div", { class: "tour-bubble", role: "dialog", "aria-modal": "true", "aria-labelledby": "tour-title", tabindex: "-1" });
  const layer = h("div", { class: "tour" }, spot, bubble);
  let index = 0;

  function close(finished = true) {
    if (finished) markSeen(name);
    layer.remove();
    removeEventListener("resize", place);
    document.removeEventListener("keydown", onKey, true);
    active = null;
    if (returnFocus && document.contains(returnFocus)) returnFocus.focus({ preventScroll: true });
  }

  function place() {
    const step = usable[index];
    const target = step.target && document.querySelector(step.target);
    const pad = 10;
    if (!target) {
      spot.classList.add("is-center");
      Object.assign(spot.style, { left: `${innerWidth / 2}px`, top: `${innerHeight / 2}px`, width: "0px", height: "0px" });
      const b = bubble.getBoundingClientRect();
      Object.assign(bubble.style, { left: `${(innerWidth - b.width) / 2}px`, top: `${Math.max(16, (innerHeight - b.height) / 2)}px` });
      return;
    }
    spot.classList.remove("is-center");
    const r = target.getBoundingClientRect();
    const box = {
      left: Math.max(4, r.left - pad), top: Math.max(4, r.top - pad),
      width: Math.min(innerWidth - 8, r.width + pad * 2), height: Math.min(innerHeight - 8, r.height + pad * 2),
    };
    Object.assign(spot.style, { left: `${box.left}px`, top: `${box.top}px`, width: `${box.width}px`, height: `${box.height}px` });
    const b = bubble.getBoundingClientRect();
    const below = box.top + box.height + 14;
    const above = box.top - b.height - 14;
    let top = below + b.height < innerHeight - 8 ? below : above > 8 ? above : Math.max(8, innerHeight - b.height - 8);
    let left = Math.min(Math.max(8, box.left + box.width / 2 - b.width / 2), innerWidth - b.width - 8);
    if (top < box.top + box.height && top + b.height > box.top && box.width > innerWidth * 0.6) {
      top = Math.max(8, innerHeight - b.height - 8);
    }
    Object.assign(bubble.style, { left: `${left}px`, top: `${top}px` });
  }

  function show() {
    const step = usable[index];
    const last = index === usable.length - 1;
    bubble.replaceChildren(
      h("div", { class: "tour-bubble-head" }, byte("idle", "", { size: "md" }), h("h2", { id: "tour-title" }, step.title)),
      h("p", {}, step.text),
      h("div", { class: "tour-actions" },
        h("span", { class: "tour-count" }, `${index + 1} of ${usable.length}`),
        !last && h("button", { type: "button", class: "button button-quiet", onclick: () => close() }, "Skip"),
        index > 0 && h("button", { type: "button", class: "button", onclick: () => go(index - 1) }, icon("arrow-left"), "Back"),
        h("button", { type: "button", class: "button button-primary", "data-next": "", onclick: () => (last ? close() : go(index + 1)) },
          last ? "Got it" : "Next", !last && icon("arrow-right"))));
    bubble.style.animation = "none";
    void bubble.offsetWidth;
    bubble.style.animation = "";
    const target = step.target && document.querySelector(step.target);
    if (target) {
      const r = target.getBoundingClientRect();
      if (r.top < 80 || r.bottom > innerHeight - 40) target.scrollIntoView({ block: "center", behavior: "instant" });
    }
    requestAnimationFrame(() => {
      place();
      bubble.querySelector("[data-next]").focus({ preventScroll: true });
    });
  }

  function go(next) {
    index = next;
    show();
  }

  function onKey(e) {
    if (e.key === "Escape") {
      e.preventDefault();
      close();
    } else if (e.key === "Tab") {
      const focusable = [...bubble.querySelectorAll("button")];
      const i = focusable.indexOf(document.activeElement);
      if (e.shiftKey && i <= 0) { e.preventDefault(); focusable.at(-1).focus(); }
      else if (!e.shiftKey && i === focusable.length - 1) { e.preventDefault(); focusable[0].focus(); }
    } else if (e.key === "ArrowRight" && !e.target.closest("input, textarea")) {
      e.preventDefault();
      if (index < usable.length - 1) go(index + 1);
    } else if (e.key === "ArrowLeft" && !e.target.closest("input, textarea")) {
      e.preventDefault();
      if (index > 0) go(index - 1);
    }
  }

  document.body.append(layer);
  addEventListener("resize", place);
  document.addEventListener("keydown", onKey, true);
  active = { close: () => close(false) };
  show();
}
