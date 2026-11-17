import { api } from "./api.js";
import { h, icon } from "./dom.js";
import { topicKind } from "./status.js";
import { enhanceContent } from "./kb.js";
import { startTourOnce } from "./tour.js";
import { listenBar } from "./speech.js";

const KIND_LABEL = { incident: "Incident", feature: "Feature" };
const pad = (n) => String(n).padStart(2, "0");

/** Tickets with notes: the finished ones, or every ticket in author mode. In course order. */
function available(catalog) {
  return catalog.topics.filter((t) => t.progress.mastered || catalog.authorMode);
}

function moduleLabel(catalog, moduleId) {
  const index = catalog.modules.findIndex((m) => m.id === moduleId);
  return `Module ${pad(Math.max(0, index))} · ${catalog.modules[index]?.title ?? ""}`;
}

/** /notes: every finished ticket, grouped by module, with a filter. */
export async function renderNotesIndex(view, catalog) {
  const topics = available(catalog);
  const head = h("header", { class: "page-head" },
    h("p", { class: "eyebrow" }, "Quick reference"),
    h("h1", {}, "Your notes"),
    h("p", { class: "muted" }, "A short page for every ticket you've finished: key points, syntax, new words and common mistakes. Come back whenever you need a reminder."));

  if (topics.length === 0) {
    const next = catalog.topics.find((t) => t.id === catalog.frontierId);
    view.replaceChildren(h("div", { class: "notes" }, head,
      h("section", { class: "empty-card" },
        icon("book-open"),
        h("h2", {}, "No notes yet"),
        h("p", {}, "Finish your first ticket and its quick reference appears here."),
        next && h("a", { class: "button button-primary", href: `/t/${next.id}` }, "Go to my ticket", icon("arrow-right")))));
    return () => {};
  }

  const search = h("input", { type: "search", class: "notes-search", id: "notes-search", placeholder: "Filter by title or ticket key",
    "aria-label": "Filter your notes", autocomplete: "off" });
  const empty = h("p", { class: "empty-line", hidden: true }, "No notes match that.");
  const groups = catalog.modules.map((module) => {
    const own = topics.filter((t) => t.moduleId === module.id);
    if (own.length === 0) return null;
    const rows = own.map((t) => {
      const kind = topicKind(t);
      const row = h("li", {},
        h("a", { class: "notes-row", href: `/notes/${t.id}` },
          h("span", { class: "notes-key" }, t.ticket.key),
          h("span", { class: "notes-title" }, t.title, h("span", { class: "notes-ticket" }, t.ticket.title)),
          kind !== "ticket" ? h("span", { class: `tag tag-${kind}` }, KIND_LABEL[kind]) : h("span"),
          icon("chevron-right")));
      row.dataset.search = `${t.ticket.key} ${t.title} ${t.ticket.title}`.toLowerCase();
      return row;
    });
    const group = h("section", { class: "notes-group", "aria-labelledby": `notes-${module.id}` },
      h("h2", { id: `notes-${module.id}` }, moduleLabel(catalog, module.id),
        h("span", { class: "mono muted" }, `${own.length}`)),
      h("ul", { class: "notes-rows" }, rows));
    return { group, rows };
  }).filter(Boolean);

  search.addEventListener("input", () => {
    const query = search.value.trim().toLowerCase();
    let shown = 0;
    for (const { group, rows } of groups) {
      let inGroup = 0;
      for (const row of rows) {
        row.hidden = query !== "" && !row.dataset.search.includes(query);
        if (!row.hidden) inGroup++;
      }
      group.hidden = inGroup === 0;
      shown += inGroup;
    }
    empty.hidden = shown > 0;
  });

  view.replaceChildren(h("div", { class: "notes" }, head,
    h("div", { class: "notes-toolbar" }, search,
      h("span", { class: "mono muted" }, `${topics.length} ${topics.length === 1 ? "ticket" : "tickets"}`)),
    h("div", { class: "notes-list", id: "notes-list" }, groups.map((g) => g.group), empty)));
  startTourOnce("notes");
  return () => {};
}

/** /notes/<id>: one ticket's quick reference. */
export async function renderNotes(view, catalog, id) {
  const summary = catalog.topics.find((t) => t.id === id);
  if (!summary) throw new Error(`There is no ticket called ${id}.`);
  const topics = available(catalog);
  const index = topics.findIndex((t) => t.id === id);
  if (index < 0) {
    view.replaceChildren(h("div", { class: "locked-page" },
      h("span", { class: "done-check is-locked", "aria-hidden": "true" }, icon("book-open")),
      h("h1", {}, "Notes open when you finish"),
      h("p", {}, `${summary.ticket.key} "${summary.ticket.title}" gets its quick reference once you finish the ticket.`),
      h("div", { class: "button-row", style: "justify-content:center" },
        h("a", { class: "button", href: "/notes" }, icon("book-open"), "All notes"),
        !summary.locked && h("a", { class: "button button-primary", href: `/t/${id}` }, "Open the ticket", icon("arrow-right")))));
    return () => {};
  }

  const notes = await api.notes(id);
  const previous = topics[index - 1];
  const next = topics[index + 1];

  const body = h("div", { class: "notes-body" },
    notes.keyPointsHtml && section("Key points", "list-checks", h("div", { html: notes.keyPointsHtml })),
    notes.syntax.length > 0 && section("Syntax", "code", notes.syntax.map((s) => h("div", { class: "notes-item notes-syntax" },
      h("h3", { html: s.titleHtml }),
      h("pre", {}, h("code", { class: "language-java" }, s.code)),
      s.noteHtml && h("div", { class: "notes-note", html: s.noteHtml })))),
    notes.wordsHtml.length > 0 && section("Words to know", "book-open",
      h("ul", { class: "notes-words" }, notes.wordsHtml.map((w) => h("li", { html: w })))),
    notes.mistakes.length > 0 && section("Common mistakes", "triangle-alert", notes.mistakes.map((m) => h("div", { class: "notes-item notes-mistake" },
      h("h3", { html: m.titleHtml }),
      m.code && h("pre", {}, h("code", { class: "language-java" }, m.code)),
      m.fixHtml && h("div", { class: "notes-note", html: m.fixHtml })))));

  const page = h("article", { class: "notes notes-page", "aria-labelledby": "notes-title" },
    h("a", { class: "back-link", href: "/notes" }, icon("arrow-left"), "All notes"),
    h("header", { class: "page-head" },
      h("p", { class: "eyebrow" }, `${summary.ticket.key} · ${moduleLabel(catalog, summary.moduleId)}`),
      h("h1", { id: "notes-title" }, summary.title),
      h("p", { class: "muted" }, `Quick reference for "${summary.ticket.title}".`),
      h("div", { class: "button-row notes-actions" },
        listenBar(() => body, { label: "Listen to all", what: "all these notes" }),
        h("a", { class: "button", href: `/t/${id}` }, icon("book-open"), "Open the full lesson"),
        h("button", { type: "button", class: "button button-quiet", onclick: () => print() }, icon("save"), "Print or save as PDF"))),
    body,
    h("nav", { class: "notes-pager", "aria-label": "More notes" },
      previous ? h("a", { class: "button", href: `/notes/${previous.id}` }, icon("arrow-left"), previous.title) : h("span"),
      next && h("a", { class: "button", href: `/notes/${next.id}` }, next.title, icon("arrow-right"))));

  view.replaceChildren(page);
  document.title = `${summary.title} · Notes · JavaQuest`;
  const dispose = enhanceContent(body);
  scrollTo({ top: 0 });
  return dispose;
}

function section(title, iconName, ...content) {
  const box = h("section", { class: "notes-section" });
  box.append(h("div", { class: "notes-section-head" },
    h("h2", {}, icon(iconName), title),
    listenBar(() => box, { what: `the ${title.toLowerCase()}` })), ...content.flat(Infinity).filter(Boolean));
  return box;
}
