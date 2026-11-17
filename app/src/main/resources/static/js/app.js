import { api } from "./api.js";
import { renderJourney } from "./journey.js";
import { renderLesson } from "./lesson.js";
import { renderCareer } from "./career.js";
import { renderReview } from "./review.js";
import { renderNotes, renderNotesIndex } from "./notes.js";
import { refreshRewards } from "./rewards.js";
import { h, setTeamPictures, pictureFor } from "./dom.js";
import { byte, setHelperPicture, HELPER_PICTURES } from "./byte.js";
import { bus } from "./events.js";
import { replayPageTour, setPageTour } from "./tour.js";
import { mountDailyLimit } from "./daily.js";
import { stopAll } from "./speech.js";

const view = document.getElementById("view");
let cleanup = () => {};

/** Routes are real paths, such as /t/module-01-basics/topic-01-variables; the server answers each with this page. */
function parseRoute() {
  const path = location.pathname.replace(/\/+$/, "") || "/";
  const params = new URLSearchParams(location.search);
  const ticket = path.match(/^\/t\/(module-[^/]+\/[^/]+)$/);
  if (ticket) return { name: "ticket", id: ticket[1], params };
  const review = path.match(/^\/review\/(module-[^/]+\/[^/]+)$/);
  if (review) return { name: "review", id: review[1] };
  const notes = path.match(/^\/notes\/(module-[^/]+\/[^/]+)$/);
  if (notes) return { name: "notes", id: notes[1] };
  if (path === "/notes") return { name: "notes" };
  if (path === "/career") return { name: "career" };
  return { name: "journey" };
}

// The learner's own team pictures, if any, are known before the first page draws its avatars.
// The helper's picture (gojo.jpg) replaces the drawn mark, so the logo is drawn again once it is known.
const picturesReady = api.teamPictures().then((files) => {
  setTeamPictures(files);
  setHelperPicture(pictureFor(files, HELPER_PICTURES));
  document.querySelector(".wordmark .byte")?.replaceWith(byte("idle", "", { size: "sm" }));
}).catch(() => {});

let lastRoute = "";
async function route({ keepFocus = false } = {}) {
  const current = parseRoute();
  // The lesson player updates ?step= itself; that is not a new page.
  const key = current.name === "ticket" ? `ticket:${current.id}` : location.pathname + location.search;
  if (key === lastRoute && current.name === "ticket") return;
  lastRoute = key;
  stopAll();
  cleanup();
  cleanup = () => {};
  for (const link of document.querySelectorAll("[data-nav]")) {
    if (link.dataset.nav === current.name) link.setAttribute("aria-current", "page");
    else link.removeAttribute("aria-current");
  }
  view.replaceChildren(h("p", { class: "loading" }, "Loading…"));
  try {
    const [catalog] = await Promise.all([api.catalog(), picturesReady]);
    document.getElementById("author-banner").hidden = !catalog.authorMode;
    document.title = { journey: "Your journey · JavaQuest", career: "Career · JavaQuest", review: "Review · JavaQuest", notes: "Notes · JavaQuest" }[current.name] ?? "JavaQuest";
    setPageTour(current.name === "ticket" ? "lesson" : current.name);
    cleanup = current.name === "ticket" ? await renderLesson(view, catalog, current.id, current.params)
      : current.name === "review" ? await renderReview(view, catalog, current.id)
      : current.name === "career" ? await renderCareer(view)
      : current.name === "notes" ? await (current.id ? renderNotes(view, catalog, current.id) : renderNotesIndex(view, catalog))
      : await renderJourney(view, catalog);
    refreshResume();
    if (!keepFocus && current.name !== "ticket" && (document.activeElement === document.body || !view.contains(document.activeElement))) {
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
mountDailyLimit(document.getElementById("daily-limit-slot"));

/**
 * The Continue tab: back to the last screen of an unfinished ticket, from any page. Hidden when there is none, or
 * while that ticket is already on screen.
 */
async function refreshResume() {
  const link = document.getElementById("resume-link");
  const resume = await api.resume().catch(() => null);
  const here = parseRoute();
  if (!resume || (here.name === "ticket" && here.id === resume.topicId)) {
    link.hidden = true;
    return;
  }
  const where = `${resume.key}, card ${resume.step + 1} of ${resume.total}: ${resume.label}`;
  link.href = `/t/${resume.topicId}`;
  link.title = `Continue ${where}`;
  link.setAttribute("aria-label", `Continue where you left off: ${where}`);
  link.hidden = false;
}
for (const name of ["resume", "career"]) bus.addEventListener(name, () => refreshResume());
// The course page shows the daily limit, and a new limit can open or close the next ticket: draw the page again,
// leaving the keyboard focus in the top bar. A lesson in progress is redrawn only when its access changed.
bus.addEventListener("settings", (e) => {
  if (parseRoute().name !== "journey" && !e.detail?.reachedChanged) return;
  lastRoute = "";
  route({ keepFocus: true });
});
setupTheme();
setupEvents();
refreshRewards();

/** Opens a page of the app without reloading it. */
function navigate(url) {
  history.pushState(null, "", url);
  route();
  scrollTo({ top: 0 });
}

// Links to app pages ("/notes", "/t/...") open in place. New tabs, downloads, the API and other sites load normally.
document.addEventListener("click", (event) => {
  if (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
  const link = event.target.closest?.("a[href]");
  if (!link || link.target || link.hasAttribute("download")) return;
  const raw = link.getAttribute("href");
  const url = new URL(raw.startsWith("#/") ? raw.slice(1) : raw, location.href);
  if (url.origin !== location.origin || url.pathname.startsWith("/api/") || !/^#?\//.test(raw)) return;
  event.preventDefault();
  if (url.pathname + url.search !== location.pathname + location.search) navigate(url.pathname + url.search);
});
addEventListener("popstate", () => route());

// Links from before the switch to paths, such as /#/t/..., still work: move them to their path.
if (location.hash.startsWith("#/")) history.replaceState(null, "", location.hash.slice(1));
route();
