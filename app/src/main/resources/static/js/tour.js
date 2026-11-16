// First-visit walkthroughs. Byte spotlights one thing at a time and explains it in a bubble.
// Each page's tour plays once (remembered in this browser); the ? button in the top bar replays it.

import { h, icon } from "./dom.js";
import { byte } from "./byte.js";

const TOURS = {
  journey: [
    { title: "Hi, I'm Byte!", text: "Welcome to JavaQuest. You're the new intern at Spoke, a bike-sharing startup. Let me show you around.", mood: "cheering" },
    { target: "#up-next", title: "Your next ticket", text: "This is the one ticket to work on now. Tickets open one at a time, so you always know where you are." },
    { target: "#current-stop", title: "Your journey", text: "Each circle is a project. Finish its tickets to open the next project. Point at a stone to see its ticket." },
    { target: "#career-chip", title: "Level and streak", text: "Every ticket earns XP and moves you up the career ladder. Learn a little every day to grow your streak." },
    { target: "#tour-button", title: "Need a reminder?", text: "Press this on any page and I'll walk you through it again.", mood: "encouraging" },
  ],
  lesson: [
    { target: "#lesson-progress", title: "A lesson in small steps", text: "Every ticket is a few short cards. This bar shows where you are, and you can jump back to any card you've seen." },
    { target: "#lesson-card", title: "One idea per card", text: "Read the card, then press Next. The arrow keys work too." },
    { target: ".anim", title: "Watch it happen", text: "Animations show code running one step at a time. Press Watch, then go at your own pace with the step buttons.", optional: true },
    { target: "#helper-fab", title: "Stuck? Ask me", text: "Open the chat for hints. More hints open as you try.", mood: "encouraging" },
    { target: "#player-next", title: "Off you go!", text: "Press Next whenever you're ready.", mood: "cheering" },
  ],
  check: [
    { target: ".quiz", title: "The onboarding check", text: "A short mixed set of questions. 80% passes, and every retry gives you a fresh mix." },
    { target: ".question", title: "Read each question", text: "If you miss one, I'll link you straight back to the card that explains it." },
  ],
  code: [
    { target: ".editor-frame", title: "Fix the code", text: "Edit here, or open the same file in your IDE. Saving in your IDE runs the checks too." },
    { target: "#run-checks", title: "Run the checks", text: "Real JUnit tests check your code. Make every check green to finish the ticket." },
    { target: ".checks", title: "Read the red ones", text: "Each failing check says what it expected. Start with the first red one.", mood: "encouraging" },
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
      h("div", { class: "tour-bubble-head" }, byte(step.mood ?? "idle", "", { size: "md", wave: index === 0 }), h("h2", { id: "tour-title" }, step.title)),
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
