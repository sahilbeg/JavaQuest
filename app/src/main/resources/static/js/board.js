import { h, icon, avatar, TEAM } from "./dom.js";
import { byte } from "./byte.js";
import { bus } from "./events.js";
import { ticketState, nextTicket } from "./status.js";

export function dockRow(topics, currentId) {
  const done = topics.filter((t) => ticketState(t).key === "done").length;
  return h("div", { class: "dock-wrap" },
    h("div", { class: "dock", role: "img", "aria-label": `${done} of ${topics.length} tickets in this project are done` },
      topics.map((t) => {
        const state = ticketState(t).key === "done" ? "done" : t.id === currentId ? "current" : "open";
        return h("span", { class: "dock-slot", "data-state": state, "data-topic": t.id, title: t.ticket.key }, icon("bike"));
      })),
    h("p", { class: "dock-caption" }, `${done} of ${topics.length} bikes docked`));
}

function ticketCard(topic) {
  const state = ticketState(topic);
  const from = TEAM[topic.ticket.from];
  return h("li", {},
    h("a", { class: "ticket-card", href: `#/t/${topic.id}` },
      h("span", { class: "ticket-card-title" }, topic.ticket.title),
      h("span", { class: `chip ${state.chip}` }, state.label),
      h("span", { class: "ticket-card-meta" },
        h("span", { class: "key" }, topic.ticket.key),
        avatar(topic.ticket.from),
        h("span", { class: "visually-hidden" }, `from ${from.name}`))));
}

export async function renderBoard(view, catalog) {
  const { modules, topics } = catalog;
  const current = nextTicket(topics);
  const currentModule = modules.find((m) => m.id === current?.moduleId) ?? modules[0];
  const moduleTopics = topics.filter((t) => t.moduleId === currentModule?.id);
  const allDone = topics.length > 0 && topics.every((t) => ticketState(t).key === "done");

  const continueBlock = current && h("section", { class: "continue", "aria-labelledby": "continue-title" },
    allDone ? h("div", {},
      h("h1", { id: "continue-title" }, `${currentModule.project}: every ticket closed`),
      h("p", { class: "continue-body" }, "Nice work. New tickets land here as soon as the next project is written. You can reopen any closed ticket below to practise again."))
    : h("div", {},
      h("h1", { id: "continue-title" }, current.ticket.title),
      h("div", { class: "continue-meta" },
        h("span", { class: "key" }, current.ticket.key),
        avatar(current.ticket.from),
        h("span", {}, `from ${TEAM[current.ticket.from].name}`),
        h("span", { class: `chip ${ticketState(current).chip}` }, ticketState(current).label)),
      h("p", { class: "continue-body" }, current.ticket.body)),
    h("div", { class: "continue-side" },
      dockRow(moduleTopics, current.id),
      !allDone && h("a", { class: "button button-primary", href: `#/t/${current.id}` },
        ticketState(current).key === "progress" ? "Continue ticket" : "Pick up ticket", icon("arrow-right"))));

  const projects = h("div", { class: "projects" },
    modules.map((module) => {
      const own = topics.filter((t) => t.moduleId === module.id);
      return h("section", { class: "project", "aria-labelledby": `p-${module.id}` },
        h("h2", { id: `p-${module.id}` }, module.project),
        h("p", { class: "project-sub" }, module.title),
        own.length
          ? h("ol", { class: "ticket-list" }, own.map(ticketCard))
          : h("p", { class: "empty" }, "No tickets in this project yet."));
    }));

  view.replaceChildren(h("div", { class: "board" },
    h("div", { class: "board-greeting" },
      byte("idle"),
      h("p", {}, h("strong", {}, "Byte: "), currentModule?.mascotIntro ?? "Welcome to Spoke!")),
    continueBlock,
    h("h2", { class: "visually-hidden" }, "Projects"),
    projects));

  const refresh = (e) => {
    const slot = view.querySelector(`.dock-slot[data-topic="${CSS.escape(e.detail.topicId)}"]`);
    if (slot && e.detail.result.outcome === "PASSED" && slot.dataset.state !== "done") {
      slot.dataset.state = "done";
      slot.setAttribute("data-docking", "");
    }
  };
  bus.addEventListener("run-finished", refresh);
  return () => bus.removeEventListener("run-finished", refresh);
}
