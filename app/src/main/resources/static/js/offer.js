import { h, icon, avatar, TEAM } from "./dom.js";
import { api } from "./api.js";
import { topicKind } from "./status.js";
import { startTourOnce } from "./tour.js";

/** "2027-02-01" → "1 February 2027", in the learner's language. Dates are local days, so no time zone shift. */
function longDate(iso) {
  const [year, month, day] = iso.split("-").map(Number);
  return new Date(year, month - 1, day).toLocaleDateString(undefined, { day: "numeric", month: "long", year: "numeric" });
}

function stat(value, label) {
  return h("div", { class: "offer-stat" }, h("p", { class: "offer-stat-value" }, value), h("p", { class: "offer-stat-label" }, label));
}

/** Counts from the catalog and the career summary, shared by the waiting page and the letter. */
function tally(catalog, summary) {
  const { modules, topics } = catalog;
  const closed = topics.filter((t) => t.progress.mastered);
  const shipped = modules.filter((m) => {
    const own = topics.filter((t) => t.moduleId === m.id);
    return own.length > 0 && own.every((t) => t.progress.mastered);
  });
  const kudos = summary.kudos.filter((k) => k.earned);
  return {
    closed: closed.length,
    total: topics.length,
    incidents: closed.filter((t) => topicKind(t) === "incident").length,
    features: closed.filter((t) => topicKind(t) === "feature").length,
    shipped,
    kudos: kudos.length,
    kudosTotal: summary.kudos.length,
  };
}

/** Before the capstone: what the letter needs, and the way back to work. */
function waiting(view, catalog, summary, counts) {
  const capstone = catalog.topics.find((t) => t.id === "module-10-professional/mini-project");
  view.replaceChildren(h("div", { class: "offer offer-waiting" },
    h("header", { class: "page-head" },
      h("p", { class: "eyebrow" }, "Your offer letter"),
      h("h1", {}, "Not yet, but it's coming."),
      h("p", { class: "muted" }, "Tsunade writes your full-time offer the day Spoke launches in Bramford. ",
        capstone ? h("span", {}, "That's the last ticket in the course: ", h("strong", {}, capstone.ticket.title), ".")
          : "That's the capstone, the last ticket in the course.")),
    h("div", { class: "offer-stats" },
      stat(`${counts.closed}/${counts.total}`, "tickets closed"),
      stat(`${counts.shipped.length}/${catalog.modules.length}`, "projects shipped"),
      stat(`${summary.level.title}`, `level ${summary.level.level}`)),
    h("div", { class: "button-row" },
      h("a", { class: "button button-primary button-big", href: "/" }, "Back to the course", icon("arrow-right")))));
}

/** After the capstone: the letter itself, printable. */
function letter(view, catalog, summary, counts) {
  const javaPro = summary.kudos.find((k) => k.id === "java-pro");
  const fullTime = summary.kudos.find((k) => k.id === "full-time");
  const left = counts.total - counts.closed;
  const from = TEAM.maya;
  view.replaceChildren(h("div", { class: "offer" },
    h("article", { class: "offer-letter", "aria-labelledby": "offer-title" },
      h("header", { class: "offer-head" },
        h("p", { class: "eyebrow" }, h("span", {}, icon("bike"), " Spoke"), h("span", { class: "sep" }, "·"),
          javaPro.earnedOn ? longDate(javaPro.earnedOn) : "Offer letter"),
        h("h1", { id: "offer-title" }, "Welcome to Spoke, full time."),
        h("div", { class: "offer-from" }, avatar("maya"), h("span", {}, `From ${from.name}, lead engineer`))),
      h("div", { class: "offer-body" },
        h("p", {}, "You walked in on day one as an intern who had never written a program. ",
          "You fixed the kiosk, modelled the fleet, kept rush hour safe, built the Spoke API, and grew it to a city of riders."),
        h("p", {}, "Then you launched Spoke in Bramford. A real service, from the design doc to the last green check."),
        h("p", {}, "So it gives me real pleasure to offer you a full-time role at Spoke as a ",
          h("strong", {}, "Software Engineer II"), ". You own features end to end, you review your teammates' code, and you're on the on-call rota with the rest of us."),
        h("p", {}, "Welcome to the team. Properly, this time.")),
      h("div", { class: "offer-stats" },
        stat(`${counts.closed}/${counts.total}`, "tickets closed"),
        stat(String(counts.incidents), counts.incidents === 1 ? "incident resolved" : "incidents resolved"),
        stat(String(counts.features), counts.features === 1 ? "feature shipped" : "features shipped"),
        stat(`${summary.level.xp}`, `XP · ${summary.level.title}`),
        stat(String(summary.streak.longest), "longest streak, in days"),
        stat(`${counts.kudos}/${counts.kudosTotal}`, "kudos")),
      h("section", { class: "offer-shipped", "aria-labelledby": "shipped-title" },
        h("h2", { id: "shipped-title" }, "What you shipped"),
        h("ol", { class: "offer-projects" }, catalog.modules.map((m) => {
          const done = counts.shipped.includes(m);
          return h("li", { class: done ? "is-done" : "" },
            done ? icon("check") : icon("clock"),
            h("span", {}, h("strong", {}, m.project), ` · ${m.title}`),
            !done && h("span", { class: "visually-hidden" }, ", tickets still open"));
        }))),
      fullTime?.earned
        ? h("p", { class: "offer-note" }, icon("star"), " Every ticket closed. You earned the ", h("strong", {}, "Full-time offer"), " kudos too.")
        : h("p", { class: "offer-note" }, `${left} ${left === 1 ? "ticket is" : "tickets are"} still open. Close them all for the `,
            h("strong", {}, "Full-time offer"), " kudos."),
      h("footer", { class: "offer-sign" }, h("p", { class: "offer-signature" }, from.name), h("p", { class: "muted" }, "Lead engineer, Spoke"))),
    h("div", { class: "button-row offer-actions" },
      h("button", { type: "button", class: "button button-primary", onclick: () => print() }, icon("save"), "Print or save as PDF"),
      h("a", { class: "button", href: "/career" }, icon("sparkles"), "Your career page"),
      h("a", { class: "button button-quiet", href: "/" }, "Back to the course"))));
}

/** The completion screen: the full-time offer letter once the capstone is done, and what is left before it. */
export async function renderOffer(view, catalog) {
  const summary = await api.career();
  const counts = tally(catalog, summary);
  if (summary.kudos.find((k) => k.id === "java-pro")?.earned) letter(view, catalog, summary, counts);
  else waiting(view, catalog, summary, counts);
  startTourOnce("offer");
  return () => {};
}
