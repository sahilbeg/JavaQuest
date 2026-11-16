import { api } from "./api.js";
import { renderJourney } from "./journey.js";
import { renderLesson } from "./lesson.js";
import { renderCareer } from "./career.js";
import { renderReview } from "./review.js";
import { refreshRewards } from "./rewards.js";
import { h } from "./dom.js";
import { byte } from "./byte.js";
import { bus } from "./events.js";
import { replayPageTour, setPageTour } from "./tour.js";
import { openSettings } from "./settings.js";

const view = document.getElementById("view");
let cleanup = () => {};

function parseRoute() {
  const [path, query = ""] = location.hash.replace(/^#/, "").split("?");
  const params = new URLSearchParams(query);
  const ticket = path.match(/^\/t\/(module-[^/]+\/[^/]+)$/);
  if (ticket) return { name: "ticket", id: ticket[1], params };
  const review = path.match(/^\/review\/(module-[^/]+\/[^/]+)$/);
  if (review) return { name: "review", id: review[1] };
  if (path === "/career") return { name: "career" };
  return { name: "journey" };
}

let lastRoute = "";
async function route() {
  const current = parseRoute();
  // The lesson player updates ?step= itself; that is not a new page.
  const key = current.name === "ticket" ? `ticket:${current.id}` : location.hash;
  if (key === lastRoute && current.name === "ticket") return;
  lastRoute = key;
  cleanup();
  cleanup = () => {};
  for (const link of document.querySelectorAll("[data-nav]")) {
    if (link.dataset.nav === current.name) link.setAttribute("aria-current", "page");
    else link.removeAttribute("aria-current");
  }
  view.replaceChildren(h("p", { class: "loading" }, "Loading…"));
  try {
    const catalog = await api.catalog();
    document.getElementById("author-banner").hidden = !catalog.authorMode;
    document.title = { journey: "Your journey · JavaQuest", career: "Career · JavaQuest", review: "Review · JavaQuest" }[current.name] ?? "JavaQuest";
    setPageTour(current.name === "ticket" ? "lesson" : current.name);
    cleanup = current.name === "ticket" ? await renderLesson(view, catalog, current.id, current.params)
      : current.name === "review" ? await renderReview(view, catalog, current.id)
      : current.name === "career" ? await renderCareer(view)
      : await renderJourney(view, catalog);
    if (current.name !== "ticket" && (document.activeElement === document.body || !view.contains(document.activeElement))) {
      view.focus({ preventScroll: true });
    }
  } catch (error) {
    view.replaceChildren(h("div", { class: "error-panel", role: "alert" },
      byte("encouraging", "", { size: "md" }),
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
    try { localStorage.setItem("jq-theme", root.dataset.theme); } catch { /* not remembered: fine */ }
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

document.getElementById("wordmark-byte").replaceWith(byte("idle", "", { size: "sm" }));
document.getElementById("tour-button").addEventListener("click", () => replayPageTour());
document.getElementById("settings-button").addEventListener("click", () => openSettings());
// A new daily limit can lock or open the next ticket, so draw the page again.
bus.addEventListener("settings", () => {
  lastRoute = "";
  route();
});
setupTheme();
setupEvents();
refreshRewards();
addEventListener("hashchange", route);
route();
