import { api } from "./api.js";
import { renderBoard } from "./board.js";
import { renderTicket } from "./ticket.js";
import { h } from "./dom.js";
import { bus } from "./events.js";

const view = document.getElementById("view");
let cleanup = () => {};

function parseRoute() {
  const [path, query = ""] = location.hash.replace(/^#/, "").split("?");
  const params = new URLSearchParams(query);
  const ticket = path.match(/^\/t\/(module-[^/]+\/[^/]+)$/);
  if (ticket) return { name: "ticket", id: ticket[1], tab: params.get("tab") ?? "kb" };
  return { name: "board" };
}

async function route() {
  cleanup();
  cleanup = () => {};
  const current = parseRoute();
  const boardLink = document.querySelector('[data-nav="board"]');
  if (current.name === "board") boardLink.setAttribute("aria-current", "page");
  else boardLink.removeAttribute("aria-current");
  view.replaceChildren(h("p", { class: "loading" }, "Loading…"));
  try {
    const catalog = await api.catalog();
    cleanup = current.name === "ticket"
      ? await renderTicket(view, catalog, current.id, current.tab)
      : await renderBoard(view, catalog);
  } catch (error) {
    view.replaceChildren(h("div", { class: "error-panel", role: "alert" },
      h("h1", {}, "Something went wrong"),
      h("p", {}, error.message),
      h("p", {}, "Check that JavaQuest is still running in your terminal, then reload the page.")));
  }
}

function setupTheme() {
  const root = document.documentElement;
  const button = document.getElementById("theme-toggle");
  const effective = () => root.dataset.theme === "system"
    ? (matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light")
    : root.dataset.theme;
  const label = () => button.setAttribute("aria-label", `Switch to ${effective() === "dark" ? "light" : "dark"} theme`);
  button.addEventListener("click", () => {
    root.dataset.theme = effective() === "dark" ? "light" : "dark";
    localStorage.setItem("jq-theme", root.dataset.theme);
    label();
    bus.dispatchEvent(new Event("theme"));
  });
  matchMedia("(prefers-color-scheme: dark)").addEventListener("change", () => {
    label();
    bus.dispatchEvent(new Event("theme"));
  });
  label();
}

function setupEvents() {
  const source = new EventSource("/api/events");
  for (const name of ["run-started", "run-finished"]) {
    source.addEventListener(name, (e) => bus.dispatchEvent(new CustomEvent(name, { detail: JSON.parse(e.data) })));
  }
}

setupTheme();
setupEvents();
addEventListener("hashchange", route);
route();
