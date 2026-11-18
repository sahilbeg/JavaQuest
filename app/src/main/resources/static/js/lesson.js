// The lesson player. A ticket's KB page is split into short cards (one idea each), followed by the
// onboarding check, the code to fix, and a celebration when the ticket is done.
//
// Steps: ticket intro (with a "you'll learn" overview from the Recap) -> learn cards (one per ### subsection,
// or per ## section without subsections) -> common mistakes (collapsed) -> recap -> check -> code -> done.

import { h, icon, avatar, TEAM, reducedMotion } from "./dom.js";
import { api } from "./api.js";
import { bus } from "./events.js";
import { focusDailyLimit } from "./daily.js";
import { listenBar, stopAll } from "./speech.js";

import { createHelper } from "./chat.js";
import { enhanceContent } from "./kb.js";
import { renderQuiz } from "./quiz.js";
import { renderFix } from "./fix.js";
import { ticketState, topicKind } from "./status.js";
import { refreshRewards } from "./rewards.js";
import { glow } from "./celebrate.js";
import { startTourOnce, setPageTour } from "./tour.js";

const slug = (heading) => heading.replace(/^#+\s*/, "").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");

const KIND = {
  incident: { label: "Incident", icon: "triangle-alert", note: "Riders are affected right now. Bronze checks resolve it. Silver and gold make the fix solid." },
  feature: { label: "Feature", icon: "sparkles", note: "A bigger build for this project. Every check must pass." },
};

/** Wide screens get the two-column lesson layout; narrower ones keep the single column in the lesson's order. */
const WIDE = matchMedia("(min-width: 1200px)");
const isSide = (n) => n.nodeType === 1 && n.matches("pre.mermaid, aside.callout");
const isFull = (n) => n.nodeType === 1 && n.matches("figure.animation, table");

/**
 * Lays a card out like a textbook page: text and code in a readable main column, diagrams and callouts beside the
 * text they belong to, and animations and tables across the whole card. Returns false when the card has nothing to
 * put beside the text, so it stays a single column.
 */
function splitCard(content) {
  const nodes = [...content.childNodes];
  if (!nodes.some(isSide)) return false;
  const head = [];
  const rows = [];
  let row = null;
  for (const node of nodes) {
    if (node.nodeType === 3 && !node.textContent.trim()) continue;
    if (node.nodeType === 1 && node.matches(".card-eyebrow") && rows.length === 0) {
      head.push(node);
    } else if (isFull(node)) {
      row = null;
      rows.push(h("div", { class: "card-full" }, node));
    } else {
      if (!row) {
        row = { main: h("div", { class: "card-main" }), side: h("div", { class: "card-side" }) };
        rows.push(h("div", { class: "card-row" }, row.main, row.side));
      }
      (isSide(node) ? row.side : row.main).append(node);
    }
  }
  for (const r of rows) {
    if (r.classList.contains("card-row") && !r.lastChild.hasChildNodes()) r.classList.add("is-plain");
  }
  content.replaceChildren(...head, ...rows);
  return true;
}

function stepStore(id) {
  const key = `jq-step:${id}`;
  return {
    get: () => { try { return Number(localStorage.getItem(key) ?? "NaN"); } catch { return NaN; } },
    set: (n) => { try { localStorage.setItem(key, String(n)); } catch { /* not saved: fine */ } },
  };
}

/** Splits the rendered lesson into cards. */
function splitLesson(html) {
  const template = document.createElement("template");
  template.innerHTML = html;
  const sections = [];
  let current = null;
  for (const node of [...template.content.childNodes]) {
    if (node.nodeType === Node.TEXT_NODE && !node.textContent.trim()) continue;
    if (node.nodeName === "H2") {
      current = { h2: node, nodes: [] };
      sections.push(current);
    } else if (current) {
      current.nodes.push(node);
    } else {
      sections.push(current = { h2: null, nodes: [node] });
    }
  }
  const cards = [];
  let recap = null;
  for (const section of sections) {
    const id = section.h2?.id ?? "";
    const title = section.h2?.textContent ?? "";
    if (id === "recap") {
      recap = section;
      continue;
    }
    if (id === "common-mistakes") {
      cards.push({ key: "mistakes", label: "Watch out", eyebrow: "Common mistakes", nodes: [section.h2, ...mistakeAccordions(section.nodes)] });
      continue;
    }
    const groups = [];
    let group = { h3: null, nodes: [] };
    for (const node of section.nodes) {
      if (node.nodeName === "H3") {
        if (group.h3 || group.nodes.length) groups.push(group);
        group = { h3: node, nodes: [] };
      } else {
        group.nodes.push(node);
      }
    }
    if (group.h3 || group.nodes.length) groups.push(group);
    if (groups.length <= 1) {
      cards.push({ key: "learn", label: title, eyebrow: null, nodes: [section.h2, ...section.nodes].filter(Boolean) });
      continue;
    }
    groups.forEach((g, i) => {
      const head = i === 0 ? [section.h2] : [];
      cards.push({ key: "learn", label: g.h3?.textContent ?? title, eyebrow: i === 0 ? null : title,
        nodes: [...head, g.h3, ...g.nodes].filter(Boolean) });
    });
  }
  return { cards, recap };
}

/** "**1. Title.** Body ..." paragraphs become collapsed details, so the page shows titles first. */
function mistakeAccordions(nodes) {
  const isTitle = (n) => n.nodeName === "P" && n.firstElementChild?.nodeName === "STRONG"
    && n.firstChild === n.firstElementChild && n.textContent.trim() === n.firstElementChild.textContent.trim();
  const startsWithStrong = (n) => n.nodeName === "P" && n.firstElementChild?.nodeName === "STRONG" && n.firstChild === n.firstElementChild;
  const items = [];
  let item = null;
  const intro = [];
  for (const node of nodes) {
    if (isTitle(node) || startsWithStrong(node)) {
      const strong = node.firstElementChild;
      item = { title: strong.textContent.replace(/\.$/, ""), body: [] };
      items.push(item);
      if (!isTitle(node)) {
        const rest = node.cloneNode(true);
        rest.firstElementChild.remove();
        item.body.push(rest);
      }
    } else if (item) {
      item.body.push(node);
    } else {
      intro.push(node);
    }
  }
  if (items.length === 0) return nodes;
  return [...intro, h("div", { class: "mistakes" }, items.map((m, i) =>
    h("details", { class: "mistake", open: i === 0 },
      h("summary", {}, m.title, icon("chevron-down")),
      h("div", { class: "mistake-body" }, m.body))))];
}

export async function renderLesson(view, catalog, id, params) {
  const summary = catalog.topics.find((t) => t.id === id);
  if (!summary) throw new Error(`There is no ticket called ${id}.`);
  if (summary.locked) return renderLocked(view, catalog, summary);

  let topic;
  try {
    topic = await api.topic(id);
  } catch (error) {
    if (/locked/i.test(error.message)) return renderLocked(view, catalog, summary);
    throw error;
  }

  const module = catalog.modules.find((m) => m.id === summary.moduleId);
  const person = TEAM[summary.ticket.from];
  const kind = topicKind(summary);
  const { cards, recap } = splitLesson(topic.lessonHtml);
  const recapList = recap?.nodes.find((n) => n.nodeName === "UL");

  const steps = [
    { key: "intro", label: "Your ticket" },
    ...cards,
    recap && { key: "recap", label: "Recap" },
    topic.hasQuiz && { key: "check", label: "Onboarding check" },
    summary.practiceKind === "CODE" && { key: "code", label: kind === "ticket" ? "Fix the code" : kind === "incident" ? "Fix the incident" : "Build it" },
    { key: "done", label: "Done" },
  ].filter(Boolean);
  const doneIndex = steps.length - 1;
  const lastWork = doneIndex - 1;

  const helper = createHelper(topic);
  const chat = helper;
  const store = stepStore(id);
  let mastered = summary.progress.mastered;
  let index = -1;
  let maxSeen = mastered ? doneIndex : 0;
  let dispose = () => {};
  let readSent = false;

  const progressButtons = steps.map((step, i) => h("button", {
    type: "button", "aria-label": `${i + 1}. ${step.label}`, title: step.label, onclick: () => go(i),
  }));
  const progress = h("div", { class: "progress-steps", id: "lesson-progress", role: "group", "aria-label": "Lesson steps" }, progressButtons);
  const stepCount = h("span", { class: "step-count", "aria-live": "polite" });
  const backButton = h("button", { type: "button", class: "button", onclick: () => go(index - 1) }, icon("arrow-left"), "Back");
  const nextButton = h("button", { type: "button", class: "button button-primary", id: "player-next", onclick: () => go(index + 1) });
  const stage = h("div", { class: "player-stage" });

  const player = h("div", { class: "player" },
    h("div", { class: "player-bar" },
      h("a", { class: "icon-button", href: "/", "aria-label": "Back to your journey" }, icon("x")),
      h("div", { class: "player-title" },
        h("span", { class: "key" }, `${summary.ticket.key} · ${module?.project ?? ""}`),
        h("p", {}, summary.ticket.title)),
      progress, stepCount),
    stage,
    h("nav", { class: "player-nav", "aria-label": "Lesson steps" }, backButton,
      h("span", { class: "nav-hint" }, "← → to move"), nextButton));

  view.replaceChildren(player, helper.fab, helper.drawer);
  document.title = `${summary.ticket.key} ${summary.ticket.title} · Cadence`;

  async function refreshState() {
    const fresh = await api.progress(id);
    const state = ticketState(fresh);
    if (fresh.progress.mastered && !mastered) {
      mastered = true;
      maxSeen = doneIndex;
      chat.say([kind === "incident" ? "Incident resolved! Riders can use the kiosk again."
        : topic.hasQuiz ? "Ticket closed! You passed the check and the code checks." : "Feature shipped! Every check passes.",
        `${person.name.split(" ")[0]} will be pleased.`], { mood: "cheering", quiet: true });
      // Stay on the result, so the learner sees their score or green checks, and offer the finish line.
      glow();
      updateChrome();
      nextButton.classList.add("pop");
      return;
    }
    if (fresh.progress.testsPassed && !fresh.progress.mastered && topic.hasQuiz && fresh.progress.quizBest < 80) {
      chat.say("Your code passes! Take the onboarding check to close the ticket.", { mood: "cheering" });
    }
    updateChrome(state);
  }

  async function showSection(heading) {
    const target = slug(heading);
    const at = steps.findIndex((s) => s.nodes?.some((n) => n.id === target || n.querySelector?.(`#${CSS.escape(target)}`)));
    if (at < 0) return;
    await go(at);
    const el = stage.querySelector(`#${CSS.escape(target)}`);
    if (!el) return;
    el.scrollIntoView({ behavior: reducedMotion() ? "auto" : "smooth", block: "start" });
    el.classList.remove("highlight-flash");
    void el.offsetWidth;
    el.classList.add("highlight-flash");
  }

  function card(step) {
    switch (step.key) {
      case "intro": return introCard();
      case "recap": return recapCard();
      case "done": return doneCard();
      case "check": {
        const box = h("div", {}, h("span", { class: "eyebrow card-eyebrow" }, icon("list-checks"), "Onboarding check"),
          h("h2", {}, "Show what you know"));
        const body = h("div");
        box.append(body);
        renderQuiz(body, id, chat, showSection, refreshState).then(() => startTourOnce("check", 300))
          .catch((e) => body.replaceChildren(h("p", { class: "error-panel" }, e.message)));
        return box;
      }
      case "code": {
        const box = h("div", {}, h("span", { class: "eyebrow card-eyebrow" }, icon("code"), step.label),
          h("h2", {}, kind === "incident" ? "Make the checks green, starting with bronze" : "Make every check green"));
        const body = h("div");
        box.append(body);
        let disposeFix = () => {};
        renderFix(body, topic, chat, refreshState).then((d) => { disposeFix = d; startTourOnce("code", 400); })
          .catch((e) => body.replaceChildren(h("p", { class: "error-panel" }, e.message)));
        box.dispose = () => disposeFix();
        return box;
      }
      default: {
        const box = h("div", {}, step.eyebrow && h("span", { class: "eyebrow card-eyebrow" }, step.eyebrow),
          step.nodes.map((n) => n.cloneNode(true)));
        return box;
      }
    }
  }

  function introCard() {
    const state = ticketState(summary);
    // On wide screens the overview sits beside the ticket (grid areas in app.css); otherwise it reads top to bottom.
    return h("div", { class: "intro" },
      h("div", { class: "intro-head" },
        kind !== "ticket" && h("p", { class: `card-kind card-kind-${kind}` }, icon(KIND[kind].icon), KIND[kind].label),
        h("span", { class: "eyebrow" }, `${summary.ticket.key} · ${module?.project ?? ""}`),
        h("h1", {}, summary.ticket.title)),
      h("div", { class: "intro-message ticket-message" },
        avatar(summary.ticket.from, "avatar-lg"),
        h("div", { class: "bubble" },
          h("p", { class: "who" }, h("strong", {}, person.name), ` · ${person.role}`),
          h("p", {}, summary.ticket.body))),
      kind !== "ticket" && h("p", { class: "intro-note" }, KIND[kind].note),
      recapList && h("section", { class: "intro-learn" }, h("h2", {}, "In this ticket you'll learn"),
        h("ul", { class: "learn-list" }, [...recapList.querySelectorAll("li")].map((li) => h("li", { html: li.innerHTML })))),
      h("div", { class: "intro-meta" },
        h("span", { class: "chip" }, icon("clock"), `${topic.minutes} min`),
        h("span", { class: "chip" }, icon("book-open"), `${cards.length} short cards`),
        topic.hasQuiz && h("span", { class: "chip" }, icon("list-checks"), "Onboarding check"),
        summary.practiceKind === "CODE" && h("span", { class: "chip" }, icon("code"), "Code to fix"),
        h("span", { class: `chip ${state.chip}` }, state.label)),
      h("button", { type: "button", class: "button button-primary button-big intro-start", onclick: () => go(1) },
        mastered ? "Revisit the lesson" : "Start", icon("arrow-right")));
  }

  function recapCard() {
    return h("div", {},
      h("p", { class: "eyebrow" }, "Recap"),
      h("h2", {}, "What you learned"),
      h("ul", { class: "learn-list" }, [...(recapList?.querySelectorAll("li") ?? [])].map((li) => h("li", { html: li.innerHTML }))),
      steps[index + 1] && steps[index + 1].key !== "done"
        && h("p", {}, `Next: ${steps[index + 1].label.toLowerCase()}.`));
  }

  function doneCard() {
    const nextId = catalog.topics[catalog.topics.findIndex((t) => t.id === id) + 1]?.id;
    const thanks = `${person.name.split(" ")[0]} says thanks. ${summary.ticket.key} is done`;
    const message = h("p", {}, `${thanks}, and the next ticket is open.`);
    const next = nextId && h("a", { class: "button button-primary button-big", href: `/t/${nextId}` }, "Next ticket", icon("arrow-right"));
    // The catalog was read before this ticket was finished: ask again whether the daily limit now holds the next one.
    if (nextId) {
      api.catalog().then((fresh) => {
        const after = fresh.topics.find((t) => t.id === nextId);
        if (!after?.locked || after.id !== fresh.frontierId || !fresh.today.reached) return;
        message.textContent = `${thanks}. That's ${fresh.today.doneToday} ${fresh.today.doneToday === 1 ? "ticket" : "tickets"} today, your daily limit, so the next ticket opens tomorrow.`;
        next.remove();
      }).catch(() => {});
    }
    return h("div", { class: "done-card" },
      h("span", { class: "done-check", "aria-hidden": "true" }, icon("check")),
      h("p", { class: "eyebrow" }, summary.ticket.key),
      h("h1", {}, kind === "incident" ? "Incident resolved" : kind === "feature" ? "Feature shipped" : "Ticket closed"),
      message,
      h("div", { class: "button-row" },
        h("a", { class: "button", href: "/" }, icon("map"), "Back to your journey"),
        h("a", { class: "button", href: `/notes/${id}` }, icon("book-open"), "Quick reference"),
        next));
  }

  function updateChrome() {
    progressButtons.forEach((b, i) => {
      b.dataset.state = i === index ? "current" : i <= maxSeen ? "seen" : "";
      b.disabled = i > maxSeen || (i === doneIndex && !mastered);
      if (i === index) b.setAttribute("aria-current", "step");
      else b.removeAttribute("aria-current");
    });
    stepCount.textContent = `${index + 1} / ${steps.length}`;
    backButton.disabled = index <= 0;
    const atWorkEnd = index >= lastWork;
    const label = index === 0 ? "Start" : steps[index + 1]?.key === "check" ? "To the check"
      : steps[index + 1]?.key === "code" ? "To the code"
      : steps[index + 1]?.key === "done" ? "Finish the ticket"
      : "Next";
    nextButton.replaceChildren(label, icon("arrow-right"));
    nextButton.hidden = index === doneIndex || (atWorkEnd && !mastered);
  }

  // The screen is saved with the progress, so a rebuild, a hard reload or another browser opens it again.
  let positionTimer;
  function savePosition() {
    clearTimeout(positionTimer);
    positionTimer = setTimeout(() => {
      api.savePosition(id, { step: index, furthest: Math.max(maxSeen, index), total: steps.length, label: steps[index].label })
        .then(() => bus.dispatchEvent(new Event("resume")))
        .catch(() => { /* this browser still remembers the card */ });
    }, 150);
  }

  async function go(next) {
    if (next < 0 || next >= steps.length) return;
    if (next === doneIndex && !mastered) return;
    const back = next < index;
    stopAll();
    dispose();
    index = next;
    maxSeen = Math.max(maxSeen, index);
    store.set(index);
    savePosition();
    history.replaceState(null, "", `/t/${id}${index ? `?step=${index}` : ""}`);
    const step = steps[index];
    const content = card(step);
    const wide = step.key === "code";
    const split = WIDE.matches && (step.key === "intro" || ((step.key === "learn" || step.key === "mistakes") && splitCard(content)));
    const box = h("article", { class: `lesson-card${wide ? " is-wide" : ""}${split ? " is-split" : ""}${back ? " is-back" : ""}`, id: "lesson-card",
      "aria-label": step.label, tabindex: "-1" }, content);
    // Reading cards get a Listen button; the check, the code and the finish line do not.
    if (["intro", "learn", "mistakes", "recap"].includes(step.key)) {
      const bar = listenBar(() => box, { what: "this card" });
      if (bar) box.prepend(bar);
    }
    stage.replaceChildren(box);
    const cleanContent = step.key === "learn" || step.key === "mistakes" ? enhanceContent(box, chat) : () => {};
    dispose = () => { cleanContent(); content.dispose?.(); };
    updateChrome();
    if (step.key === "done" && mastered) {
      refreshRewards();
    }
    if ((step.key === "recap" || (step.key === "learn" && !recap && index === cards.length)) && !readSent) {
      readSent = true;
      api.read(id).then(refreshRewards).catch(() => {});
    }
    setPageTour(step.key === "check" ? "check" : step.key === "code" ? "code" : "lesson");
    scrollTo({ top: 0, behavior: "instant" });
    box.focus({ preventScroll: true });
  }

  const onKey = (e) => {
    if (e.defaultPrevented || e.altKey || e.metaKey || e.ctrlKey) return;
    if (e.target.closest("input, textarea, select, .monaco-editor, .anim, .helper, .tour, [contenteditable]")) return;
    if (e.key === "ArrowRight" && !nextButton.hidden && index < lastWork + (mastered ? 1 : 0)) go(index + 1);
    if (e.key === "ArrowLeft" && index > 0) go(index - 1);
  };
  document.addEventListener("keydown", onKey);
  // Crossing the wide-screen breakpoint (a resized window) draws the card again in the other layout.
  const onWide = () => go(index);
  WIDE.addEventListener("change", onWide);

  const legacyTab = { check: "check", fix: "code" }[params.get("tab")];
  const fromUrl = Number(params.get("step"));
  // Without a card in the address, open the saved screen: the server's copy first, then this browser's. A card is
  // found by its title, so a rebuild that adds or removes cards still opens the one the learner was reading.
  const remote = legacyTab || (Number.isInteger(fromUrl) && fromUrl > 0) ? null : await api.position(id).catch(() => null);
  const byLabel = remote ? steps.findIndex((s) => s.label === remote.label) : -1;
  const remoteStep = !remote ? NaN : steps[remote.step]?.label === remote.label ? remote.step : byLabel >= 0 ? byLabel : remote.step;
  if (remote) maxSeen = Math.max(maxSeen, Math.min(remote.furthest + (byLabel >= 0 ? byLabel - remote.step : 0), lastWork));
  const saved = Number.isInteger(remoteStep) ? remoteStep : store.get();
  const first = legacyTab ? steps.findIndex((s) => s.key === legacyTab)
    : Number.isInteger(fromUrl) && fromUrl > 0 ? fromUrl
    : Number.isInteger(saved) && saved > 0 && saved < doneIndex ? saved : 0;
  maxSeen = Math.max(maxSeen, Math.min(first, lastWork));
  await go(Math.min(first, mastered ? doneIndex : lastWork));

  chat.say([`This ticket is from ${person.name.split(" ")[0]}.`,
    "Go through the cards, then pass the check" + (summary.practiceKind === "CODE" ? " and fix the code." : ".") + " Ask me for a hint any time."],
    { fresh: false, quiet: true });
  if (index === 0) startTourOnce("lesson", 600);

  return () => {
    dispose();
    document.removeEventListener("keydown", onKey);
    WIDE.removeEventListener("change", onWide);
  };
}

function renderResting(view, catalog, summary) {
  const { doneToday, limit } = catalog.today;
  view.replaceChildren(h("div", { class: "locked-page" },
    h("span", { class: "done-check is-resting", "aria-hidden": "true" }, icon("clock")),
    h("h1", {}, "This ticket opens tomorrow"),
    h("p", {}, `You've completed ${doneToday} ${doneToday === 1 ? "ticket" : "tickets"} today, your daily limit of ${limit}. `,
      `Come back tomorrow to start ${summary.ticket.key} "${summary.ticket.title}".`),
    h("p", {}, "Until then, revisit any ticket you've finished, or do your reviews."),
    h("div", { class: "button-row", style: "justify-content:center" },
      h("a", { class: "button button-primary", href: "/" }, icon("map"), "Your journey"),
      h("button", { type: "button", class: "button", onclick: () => focusDailyLimit() }, icon("settings"), "Change daily limit"))));
  document.title = "Opens tomorrow · Cadence";
  return () => {};
}

function renderLocked(view, catalog, summary) {
  if (summary.id === catalog.frontierId && catalog.today?.reached) return renderResting(view, catalog, summary);
  const frontier = catalog.topics.find((t) => t.id === catalog.frontierId);
  view.replaceChildren(h("div", { class: "locked-page" },
    h("span", { class: "done-check is-locked", "aria-hidden": "true" }, icon("lock")),
    h("h1", {}, "This ticket is locked"),
    h("p", {}, `${summary.ticket.key} "${summary.ticket.title}" opens once the tickets before it are done. Tickets open one at a time, so each one builds on the last.`),
    frontier && h("p", {}, "Your current ticket is ", h("strong", {}, `${frontier.ticket.key} "${frontier.ticket.title}"`), "."),
    h("div", { class: "button-row", style: "justify-content:center" },
      h("a", { class: "button", href: "/" }, icon("map"), "Your journey"),
      frontier && h("a", { class: "button button-primary", href: `/t/${frontier.id}` }, "Go to my ticket", icon("arrow-right")))));
  document.title = "Locked ticket · Cadence";
  return () => {};
}
