import { h, icon, avatar, TEAM } from "./dom.js";
import { api } from "./api.js";
import { createChat } from "./chat.js";
import { renderKb } from "./kb.js";
import { renderQuiz } from "./quiz.js";
import { renderFix } from "./fix.js";
import { ticketState } from "./status.js";
import { toast } from "./dom.js";

const TABS = [
  { key: "kb", label: "KB page", icon: "book-open" },
  { key: "check", label: "Onboarding check", icon: "list-checks" },
  { key: "fix", label: "Fix", icon: "code" },
];

const slug = (heading) => heading.replace(/^#+\s*/, "").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");

export async function renderTicket(view, catalog, id, initialTab) {
  const topic = await api.topic(id);
  const summary = topic.summary;
  const module = catalog.modules.find((m) => m.id === summary.moduleId);
  const siblings = catalog.topics.filter((t) => t.moduleId === summary.moduleId);
  const person = TEAM[summary.ticket.from];
  const stateChip = h("span", { class: `chip ${ticketState(summary).chip}` }, ticketState(summary).label);
  const disposers = [];
  let wasMastered = summary.progress.mastered;

  async function refreshState() {
    const fresh = await api.progress(id);
    const state = ticketState(fresh);
    stateChip.className = `chip ${state.chip}`;
    stateChip.textContent = state.label;
    if (fresh.progress.mastered && !wasMastered) {
      chat.say([topic.kind === "incident" ? "Incident resolved! Riders can use the kiosk again."
        : topic.hasQuiz ? "Ticket closed! You passed the check and the code checks." : "Feature shipped! Every check passes.",
        `${person.name.split(" ")[0]} will be pleased.`], { mood: "cheering" });
      toast(`${summary.ticket.key} closed. Nice work!`);
    } else if (fresh.progress.testsPassed && !fresh.progress.mastered && fresh.progress.quizBest < 80) {
      chat.say("Your code passes! Take the onboarding check to close the ticket.", { mood: "cheering" });
    }
    wasMastered = fresh.progress.mastered;
  }

  const chatBox = h("aside", { class: "chat", id: "byte-chat", tabindex: "-1", "aria-label": "Chat with Byte" });
  const chat = createChat(chatBox, topic);

  const tabs = TABS.filter((t) => t.key !== "check" || topic.hasQuiz);
  const panels = Object.fromEntries(tabs.map((t) =>
    [t.key, h("div", { role: "tabpanel", id: `panel-${t.key}`, "aria-labelledby": `tab-${t.key}`, tabindex: "0", hidden: true })]));
  const rendered = new Set();
  const tabButtons = tabs.map((t) => h("button", {
    type: "button", role: "tab", class: "tab", id: `tab-${t.key}`, "aria-controls": `panel-${t.key}`,
    "aria-selected": "false", tabindex: "-1", onclick: () => select(t.key, true),
  }, icon(t.icon), t.label));
  const tablist = h("div", { class: "tabs", role: "tablist", "aria-label": "Ticket sections" }, tabButtons);
  tablist.addEventListener("keydown", (e) => {
    const index = tabButtons.indexOf(document.activeElement);
    if (index < 0 || !["ArrowLeft", "ArrowRight", "Home", "End"].includes(e.key)) return;
    e.preventDefault();
    const next = e.key === "Home" ? 0 : e.key === "End" ? tabs.length - 1
      : (index + (e.key === "ArrowRight" ? 1 : -1) + tabs.length) % tabs.length;
    select(tabs[next].key, true);
    tabButtons[next].focus();
  });

  async function select(key, updateUrl) {
    if (!panels[key]) key = "kb";
    tabs.forEach((t, i) => {
      const on = t.key === key;
      tabButtons[i].setAttribute("aria-selected", String(on));
      tabButtons[i].tabIndex = on ? 0 : -1;
      panels[t.key].hidden = !on;
    });
    if (updateUrl) history.replaceState(null, "", `#/t/${id}${key === "kb" ? "" : `?tab=${key}`}`);
    if (rendered.has(key)) return;
    rendered.add(key);
    try {
      if (key === "kb") disposers.push(renderKb(panels.kb, topic.lessonHtml, chat, id));
      if (key === "check") await renderQuiz(panels.check, id, chat, showSection, refreshState);
      if (key === "fix") disposers.push(await renderFix(panels.fix, topic, chat, refreshState));
    } catch (error) {
      rendered.delete(key);
      panels[key].replaceChildren(h("div", { class: "error-panel", role: "alert" }, h("p", {}, error.message)));
    }
  }

  async function showSection(heading) {
    await select("kb", true);
    const target = panels.kb.querySelector(`#${CSS.escape(slug(heading))}`);
    if (!target) return;
    target.scrollIntoView({ behavior: "smooth", block: "start" });
    target.classList.remove("highlight-flash");
    void target.offsetWidth;
    target.classList.add("highlight-flash");
  }

  const pagerLink = (targetId, label, iconName, after) => {
    const target = catalog.topics.find((t) => t.id === targetId);
    if (!target) return h("span");
    return h("a", { class: "button", href: `#/t/${target.id}` },
      !after && icon(iconName), `${label}: ${target.ticket.key}`, after && icon(iconName));
  };

  const kindBanner = topic.kind === "incident"
    ? h("p", { class: "incident-banner", role: "note" }, icon("triangle-alert"),
        "Incident: riders are affected right now. Bronze checks resolve it. Silver and gold make the fix solid.")
    : topic.kind === "feature"
      ? h("p", { class: "feature-banner", role: "note" }, icon("sparkles"), "Feature: a bigger build for this project. Every check must pass.")
      : null;
  const main = h("div", { class: "ticket-main" },
    kindBanner,
    h("header", { class: "ticket-head" },
      h("div", { class: "ticket-head-row" },
        h("span", { class: "key" }, summary.ticket.key),
        stateChip,
        h("span", { class: "key" }, `${topic.minutes} min read`),
        h("button", { type: "button", class: "button button-quiet chat-jump", onclick: () => chatBox.focus() || chatBox.scrollIntoView({ behavior: "smooth" }) },
          icon("message-circle"), "Chat with Byte")),
      h("h1", {}, summary.ticket.title),
      h("div", { class: "ticket-request" },
        avatar(summary.ticket.from, "avatar-lg"),
        h("div", {},
          h("p", { class: "ticket-request-from" }, h("strong", {}, person.name), ` · ${person.role}`),
          h("p", {}, summary.ticket.body)))),
    tablist,
    Object.values(panels),
    h("nav", { class: "pager", "aria-label": "Previous and next ticket" },
      pagerLink(topic.previousId, "Previous", "arrow-left", false),
      pagerLink(topic.nextId, "Next", "arrow-right", true)));

  const wide = matchMedia("(min-width: 821px)").matches;
  const sideNav = h("nav", { class: "side-nav", "aria-label": module?.project ?? "Project" },
    h("details", { open: wide },
    h("summary", { tabindex: wide ? "-1" : null }, h("h2", {}, module?.project ?? "Project")),
    h("ol", {}, siblings.map((t) => h("li", {},
      h("a", { href: `#/t/${t.id}`, "aria-current": t.id === id ? "page" : null },
        h("span", { class: "key" }, t.ticket.key),
        h("span", {}, t.ticket.title)))))));

  view.replaceChildren(h("div", { class: "ticket-page" }, sideNav, main, chatBox));
  document.title = `${summary.ticket.key} ${summary.ticket.title} · Spoke`;
  chat.say([`This one is from ${person.name.split(" ")[0]}.`,
    topic.hasQuiz
      ? "Read the KB page, pass the onboarding check, then fix the code. I'm here if you need a hint."
      : "Read the KB page, then fix the code until the checks pass. I'm here if you need a hint."], { fresh: false });
  await select(initialTab, false);
  view.focus({ preventScroll: true });
  return () => disposers.forEach((dispose) => dispose());
}
