import { h, icon, avatar, TEAM } from "./dom.js";
import { byteSays } from "./byte.js";
import { api } from "./api.js";
import { ticketState, topicKind, nextTicket, moduleEmoji } from "./status.js";
import { startTourOnce } from "./tour.js";

const KIND_LABEL = { incident: "Incident", feature: "Feature", ticket: "Ticket" };

function greeting() {
  const hour = new Date().getHours();
  return hour < 12 ? "Good morning!" : hour < 18 ? "Good afternoon!" : "Good evening!";
}

/** done | current | open | locked | soon */
function moduleState(own, catalog) {
  if (own.length === 0) return "soon";
  if (own.every((t) => t.progress.mastered)) return "done";
  if (own.some((t) => t.id === catalog.frontierId)) return "current";
  if (own.every((t) => t.locked)) return "locked";
  return "open";
}

function stone(topic, frontierId) {
  const state = topic.progress.mastered ? "done" : topic.id === frontierId ? "current" : topic.locked ? "locked" : "open";
  const kind = topicKind(topic);
  const glyph = state === "done" ? icon("check")
    : state === "locked" ? icon("lock")
    : kind === "incident" ? "⚠️" : kind === "feature" ? "✨" : icon("play");
  const label = `${topic.ticket.key}: ${topic.ticket.title}. ${state === "done" ? "Done" : state === "locked" ? "Locked" : state === "current" ? "Up next" : "Open"}.`;
  const inner = [glyph, h("span", { class: "stone-tip", "aria-hidden": "true" }, topic.ticket.title),
    state === "current" && h("span", { class: "stone-start", "aria-hidden": "true" }, "Start")];
  return h("li", {}, state === "locked"
    ? h("span", { class: "stone", "data-state": state, "data-kind": kind, role: "img", "aria-label": label, tabindex: "0" }, inner)
    : h("a", { class: "stone", "data-state": state, "data-kind": kind, href: `#/t/${topic.id}`, "aria-label": label }, inner));
}

function stop(module, own, catalog, previousProject) {
  const state = moduleState(own, catalog);
  const done = own.filter((t) => t.progress.mastered).length;
  const chip = {
    done: h("span", { class: "chip chip-done" }, icon("check"), "Done"),
    current: h("span", { class: "chip chip-progress" }, `${done} of ${own.length} done`),
    open: h("span", { class: "chip chip-todo" }, `${done} of ${own.length} done`),
    locked: h("span", { class: "chip chip-locked" }, icon("lock"), "Locked"),
    soon: h("span", { class: "chip chip-locked" }, "Coming soon"),
  }[state];
  const badge = state === "done" ? icon("check") : state === "locked" || state === "soon" ? icon("lock") : String(own.length - done);
  const stones = h("ol", { class: "stones", "aria-label": `Tickets in ${module.project}` }, own.map((t) => stone(t, catalog.frontierId)));
  const body = state === "current" ? [h("p", { class: "stop-sub" }, module.summary), stones]
    : state === "done" || state === "open" ? [h("details", {},
        h("summary", {}, state === "done" ? "Revisit tickets" : "Show tickets", icon("chevron-down")), stones)]
    : state === "locked" ? [h("p", { class: "stop-sub" }, module.summary),
        previousProject && h("p", { class: "stop-sub" }, icon("lock"), ` Finish ${previousProject} to open this project.`)]
    : [h("p", { class: "stop-sub" }, module.summary)];
  return h("li", { class: "stop", "data-state": state, id: state === "current" ? "current-stop" : null },
    h("div", { class: "stop-node", "aria-hidden": "true" }, moduleEmoji(module.id), h("span", { class: "stop-badge" }, badge)),
    h("div", { class: "stop-card" },
      h("div", { class: "stop-head" }, h("h3", {}, module.project), chip),
      h("p", { class: "stop-sub" }, module.title),
      body));
}

export async function renderJourney(view, catalog) {
  const { modules, topics } = catalog;
  const current = nextTicket(catalog);
  const currentModule = modules.find((m) => m.id === current?.moduleId) ?? modules[0];
  const own = topics.filter((t) => t.moduleId === currentModule?.id);
  const allDone = topics.length > 0 && topics.every((t) => t.progress.mastered);
  const doneHere = own.filter((t) => t.progress.mastered).length;
  const state = current ? ticketState(current) : null;
  const kind = current ? topicKind(current) : "ticket";

  const upNext = current && h("section", { class: "up-next", id: "up-next", "aria-labelledby": "up-next-title" },
    allDone
      ? [h("span", { class: "eyebrow" }, icon("sparkles"), "All caught up"),
         h("h1", { id: "up-next-title" }, "Every ticket is done!"),
         h("p", {}, "New projects land on your journey as soon as they're written. Revisit any ticket below to practise.")]
      : [h("span", { class: "eyebrow" }, icon("ticket"), `Up next · ${currentModule.project}`),
         h("h1", { id: "up-next-title" }, current.ticket.title),
         h("div", { class: "up-next-meta" },
           avatar(current.ticket.from),
           h("span", {}, `from ${TEAM[current.ticket.from].name}`),
           h("span", { class: "key" }, current.ticket.key),
           kind !== "ticket" && h("span", { class: `chip ${kind === "incident" ? "chip-failing" : "chip-todo"}` }, KIND_LABEL[kind]),
           h("span", { class: `chip ${state.chip}` }, state.label)),
         h("div", { class: "up-next-actions" },
           h("a", { class: "button button-primary button-big", href: `#/t/${current.id}` },
             state.key === "progress" ? "Continue" : "Start", icon("arrow-right")),
           h("div", { class: "module-progress" },
             h("span", { class: "meter", role: "img", "aria-label": `${doneHere} of ${own.length} tickets done in ${currentModule.project}` },
               h("span", { style: `width:${own.length ? Math.round((doneHere / own.length) * 100) : 0}%` })),
             `${doneHere} / ${own.length}`))]);

  const career = await api.career().catch(() => null);
  const due = career?.reviewsDue ?? [];
  const reviewStrip = due.length > 0 && h("section", { class: "review-strip", "aria-labelledby": "due-title" },
    h("span", { style: "font-size:1.8rem", "aria-hidden": "true" }, "🔁"),
    h("h2", { id: "due-title" }, `${due.length} ${due.length === 1 ? "review" : "reviews"} due today`),
    h("p", {}, "Three quick questions from tickets you closed, so they don't fade."),
    h("a", { class: "button button-primary", href: `#/review/${due[0].id}` }, "Start review", icon("arrow-right")));

  const path = h("ol", { class: "path", id: "journey-path" }, modules.map((module, i) =>
    stop(module, topics.filter((t) => t.moduleId === module.id), catalog, modules[i - 1]?.project)));

  view.replaceChildren(h("div", { class: "journey" },
    h("div", { class: "journey-hero" },
      byteSays([greeting(), currentModule?.mascotIntro ?? "Welcome to Spoke!"], { mood: "idle", size: "hero", wave: true }),
      upNext),
    reviewStrip,
    h("div", { class: "path-title" }, h("h2", {}, "Your journey"), h("p", {}, `${modules.length} projects, from your first program to a full-time offer.`)),
    path));

  startTourOnce("journey");
  return () => {};
}
