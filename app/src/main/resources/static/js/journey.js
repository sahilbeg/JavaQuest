import { h, icon, avatar, TEAM } from "./dom.js";
import { api } from "./api.js";
import { ticketState, topicKind, nextTicket } from "./status.js";
import { startTourOnce } from "./tour.js";
import { focusDailyLimit } from "./daily.js";

const KIND_LABEL = { incident: "Incident", feature: "Feature" };
const pad = (n) => String(n).padStart(2, "0");

function greeting() {
  const hour = new Date().getHours();
  return hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
}

/** done | current | open | locked | soon */
function moduleState(own, catalog) {
  if (own.length === 0) return "soon";
  if (own.every((t) => t.progress.mastered)) return "done";
  if (own.some((t) => t.id === catalog.frontierId)) return "current";
  if (own.every((t) => t.locked)) return "locked";
  return "open";
}

const tickets = (n) => `${n} ${n === 1 ? "ticket" : "tickets"}`;

function topicRow(topic, index, frontierId) {
  // The frontier is locked only when the daily limit is reached: it waits for tomorrow.
  const state = topic.progress.mastered ? "done"
    : topic.id === frontierId ? (topic.locked ? "resting" : "current")
    : topic.locked ? "locked" : "open";
  const kind = topicKind(topic);
  const mark = state === "done" ? icon("check") : state === "locked" ? icon("lock") : state === "resting" ? icon("clock")
    : state === "current" ? h("span", { class: "dot" }) : h("span", { class: "ring" });
  const inner = [
    h("span", { class: "topic-mark", "aria-hidden": "true" }, mark),
    h("span", { class: "topic-num" }, pad(index + 1)),
    h("span", { class: "topic-title" }, topic.ticket.title),
    kind !== "ticket" && h("span", { class: `tag tag-${kind}` }, KIND_LABEL[kind]),
    h("span", { class: "topic-key" }, topic.ticket.key),
    state === "current" && h("span", { class: "topic-go" }, "Start", icon("arrow-right")),
    state === "resting" && h("span", { class: "topic-wait" }, "Tomorrow"),
    h("span", { class: "visually-hidden" }, `, ${{ done: "done", locked: "locked", resting: "opens tomorrow", current: "up next" }[state] ?? "open"}`),
  ];
  return h("li", {}, state === "locked" || state === "resting"
    ? h("span", { class: "topic-row", "data-state": state, "aria-disabled": "true" }, inner)
    : h("a", { class: "topic-row", "data-state": state, href: `/t/${topic.id}` }, inner));
}

function moduleItem(module, own, catalog, index, previousProject) {
  const state = moduleState(own, catalog);
  const done = own.filter((t) => t.progress.mastered).length;
  const percent = own.length ? Math.round((done / own.length) * 100) : 0;
  const status = {
    done: h("span", { class: "status status-done" }, icon("check"), "Complete"),
    current: h("span", { class: "status status-current" }, h("span", { class: "dot" }), "In progress"),
    open: h("span", { class: "status" }, "Open"),
    locked: h("span", { class: "status status-locked" }, icon("lock"), "Locked"),
    soon: h("span", { class: "status status-locked" }, "Coming soon"),
  }[state];
  const head = [
    h("span", { class: "module-num" }, pad(index)),
    h("span", { class: "module-name" }, h("strong", {}, module.title), h("span", {}, module.project)),
    status,
    h("span", { class: "module-meter", "aria-hidden": "true" }, h("span", { style: `width:${percent}%` })),
    h("span", { class: "module-count" }, `${done}/${own.length}`),
  ];
  // On wide screens the tickets run down two columns (--rows per column), keeping the course order top to bottom.
  const topics = h("ol", { class: "topic-list", style: `--rows:${Math.ceil(own.length / 2)}`, "aria-label": `Tickets in ${module.title}` }, own.map((t, i) => topicRow(t, i, catalog.frontierId)));
  if (state === "locked" || state === "soon") {
    return h("li", { class: "module", "data-state": state },
      h("div", { class: "module-head" }, head),
      h("p", { class: "module-note" }, module.summary,
        state === "locked" && previousProject && h("span", {}, ` Finish ${previousProject} to unlock.`)));
  }
  return h("li", { class: "module", "data-state": state, id: state === "current" ? "current-stop" : null },
    h("details", { open: state === "current" },
      h("summary", { class: "module-head" }, head, icon("chevron-down")),
      h("p", { class: "module-note" }, module.summary),
      topics));
}

export async function renderJourney(view, catalog) {
  const { modules, topics } = catalog;
  const current = nextTicket(catalog);
  const currentModule = modules.find((m) => m.id === current?.moduleId) ?? modules[0];
  const own = topics.filter((t) => t.moduleId === currentModule?.id);
  const allDone = topics.length > 0 && topics.every((t) => t.progress.mastered);
  const doneHere = own.filter((t) => t.progress.mastered).length;
  const doneAll = topics.filter((t) => t.progress.mastered).length;
  const state = current ? ticketState(current) : null;
  const kind = current ? topicKind(current) : "ticket";
  const moduleIndex = Math.max(0, modules.indexOf(currentModule));

  const today = catalog.today;
  const resting = !allDone && current?.locked && current.id === catalog.frontierId;

  const restCard = resting && h("section", { class: "up-next is-resting", id: "up-next", "aria-labelledby": "up-next-title" },
    h("div", { class: "up-next-main" },
      h("p", { class: "eyebrow" }, `Done for today · ${tickets(today.doneToday)}`),
      h("h2", { id: "up-next-title" }, "That's today's tickets done."),
      h("p", { class: "up-next-body" }, `You've reached your daily limit of ${tickets(today.limit)}. `,
        h("strong", {}, `${current.ticket.key} "${current.ticket.title}"`), " opens tomorrow. ",
        "In the meantime, revisit any ticket you've finished, or do your reviews.")),
    h("div", { class: "up-next-side" },
      h("button", { type: "button", class: "button button-big", onclick: () => document.getElementById("journey-path")?.scrollIntoView({ behavior: "smooth", block: "start" }) },
        icon("rotate-ccw"), "Revisit a ticket"),
      h("button", { type: "button", class: "button button-quiet", onclick: () => focusDailyLimit() }, icon("settings"), "Change daily limit")));

  // Where the learner left off inside the current ticket, saved with the progress.
  const resume = await api.resume().catch(() => null);
  const resumeHere = !allDone && resume?.topicId === current?.id && resume.step > 0 ? resume : null;

  const upNext = restCard || current && h("section", { class: "up-next", id: "up-next", "aria-labelledby": "up-next-title" },
    allDone
      ? [h("p", { class: "eyebrow" }, "All caught up"),
         h("h2", { id: "up-next-title" }, "Every available ticket is done."),
         h("p", { class: "muted" }, "New modules appear here as they're released. Revisit any ticket below to practise.")]
      : [h("div", { class: "up-next-main" },
          h("p", { class: "eyebrow" }, `Up next · Module ${pad(moduleIndex)} · ${currentModule.title}`),
          h("h2", { id: "up-next-title" }, current.ticket.title),
          h("div", { class: "up-next-meta" },
            avatar(current.ticket.from),
            h("span", {}, TEAM[current.ticket.from].name),
            h("span", { class: "sep" }, "·"),
            h("span", { class: "mono" }, current.ticket.key),
            kind !== "ticket" && h("span", { class: `tag tag-${kind}` }, KIND_LABEL[kind]),
            state.key === "progress" && h("span", { class: "tag" }, state.label)),
          h("p", { class: "up-next-body" }, current.ticket.body),
          resumeHere && h("p", { class: "resume-line" }, icon("play"),
            `You were on card ${resumeHere.step + 1} of ${resumeHere.total}: `, h("strong", {}, resumeHere.label))),
        h("div", { class: "up-next-side" },
          h("a", { class: "button button-primary button-big", href: `/t/${current.id}` },
            resumeHere ? `Continue at card ${resumeHere.step + 1}` : state.key === "progress" ? "Continue" : "Start ticket", icon("arrow-right")),
          h("div", { class: "module-progress" },
            h("span", { class: "meter", role: "img", "aria-label": `${doneHere} of ${own.length} tickets done in ${currentModule.title}` },
              h("span", { style: `width:${own.length ? Math.round((doneHere / own.length) * 100) : 0}%` })),
            h("span", { class: "mono" }, `${doneHere}/${own.length} in this module`)))]);

  const career = await api.career().catch(() => null);
  const due = career?.reviewsDue ?? [];
  const reviewStrip = due.length > 0 && h("section", { class: "review-strip", "aria-labelledby": "due-title" },
    icon("rotate-ccw"),
    h("div", {}, h("h2", { id: "due-title" }, `${due.length} ${due.length === 1 ? "review" : "reviews"} due`),
      h("p", {}, "Three quick questions from tickets you closed, so they stay fresh.")),
    h("a", { class: "button", href: `/review/${due[0].id}` }, "Start review", icon("arrow-right")));

  const outline = h("ol", { class: "outline", id: "journey-path" }, modules.map((module, i) =>
    moduleItem(module, topics.filter((t) => t.moduleId === module.id), catalog, i, modules[i - 1]?.title)));

  view.replaceChildren(h("div", { class: "journey" },
    h("header", { class: "page-head" },
      h("p", { class: "eyebrow" }, greeting()),
      h("h1", {}, "Your Java course"),
      h("p", { class: "muted" }, `${doneAll} of ${topics.length} tickets complete · ${modules.length} modules, from your first program to a full-time offer.`)),
    upNext,
    reviewStrip,
    h("div", { class: "section-head" }, h("h2", {}, "Course outline"), h("span", { class: "mono muted" },
      today.limitOn && !catalog.authorMode ? `One ticket at a time · ${tickets(today.limit)} a day` : "One ticket at a time")),
    outline));

  startTourOnce("journey");
  return () => {};
}
