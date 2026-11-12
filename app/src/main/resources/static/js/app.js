import { api } from "./api.js";
import { renderBoard } from "./board.js";
import { renderTicket } from "./ticket.js";
import { renderCareer } from "./career.js";
import { renderReview } from "./review.js";
import { refreshRewards } from "./rewards.js";
import { h } from "./dom.js";
import { bus } from "./events.js";

const view = document.getElementById("view");
let cleanup = () => {};

function parseRoute() {
  const [path, query = ""] = location.hash.replace(/^#/, "").split("?");
  const params = new URLSearchParams(query);
  const ticket = path.match(/^\/t\/(module-[^/]+\/[^/]+)$/);
  if (ticket) return { name: "ticket", id: ticket[1], tab: params.get("tab") ?? "kb" };
  const review = path.match(/^\/review\/(module-[^/]+\/[^/]+)$/);
  if (review) return { name: "review", id: review[1] };
  if (path === "/career") return { name: "career" };
  return { name: "board" };
}

async function route() {
  cleanup();
  cleanup = () => {};
  const current = parseRoute();
  for (const link of document.querySelectorAll("[data-nav]")) {
    if (link.dataset.nav === current.name) link.setAttribute("aria-current", "page");
    else link.removeAttribute("aria-current");
  }
  view.replaceChildren(h("p", { class: "loading" }, "Loading…"));
  try {
    const catalog = await api.catalog();
    document.title = { board: "Board · Spoke", career: "Career · Spoke", review: "Review · Spoke" }[current.name] ?? "Spoke · JavaQuest";
    cleanup = current.name === "ticket" ? await renderTicket(view, catalog, current.id, current.tab)
      : current.name === "review" ? await renderReview(view, catalog, current.id)
      : current.name === "career" ? await renderCareer(view)
      : await renderBoard(view, catalog);
    if (document.activeElement === document.body || !view.contains(document.activeElement)) view.focus({ preventScroll: true });
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
  const banner = document.getElementById("connection");
  source.addEventListener("open", () => { banner.hidden = true; });
  source.addEventListener("error", () => {
    if (source.readyState !== EventSource.OPEN) banner.hidden = false;
  });
  for (const name of ["run-started", "run-finished"]) {
    source.addEventListener(name, (e) => bus.dispatchEvent(new CustomEvent(name, { detail: JSON.parse(e.data) })));
  }
  bus.addEventListener("run-finished", () => refreshRewards());
}

setupTheme();
setupEvents();
refreshRewards();
addEventListener("hashchange", route);
route();
