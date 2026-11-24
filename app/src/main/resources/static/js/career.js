import { h, icon, avatar, TEAM, toast } from "./dom.js";
import { api } from "./api.js";
import { refreshRewards } from "./rewards.js";

function levelBlock({ level, streak }) {
  const span = level.nextLevelXp - level.levelStartXp;
  const into = level.xp - level.levelStartXp;
  const levelsToTitle = level.nextTitleLevel - level.level;
  return h("section", { class: "career-hero", "aria-labelledby": "career-title" },
    h("div", {},
      h("h1", { id: "career-title" }, level.title),
      h("p", { class: "career-sub" }, `Level ${level.level} · ${level.xp} XP`),
      h("div", { class: "level-track", role: "progressbar", "aria-valuemin": level.levelStartXp,
          "aria-valuemax": level.nextLevelXp, "aria-valuenow": level.xp, "aria-label": "Progress to the next level" },
        h("span", { class: "level-track-fill", style: `width:${Math.round((into / span) * 100)}%` })),
      h("p", { class: "career-sub" }, `${level.nextLevelXp - level.xp} XP to level ${level.level + 1}. `,
        `${levelsToTitle} ${levelsToTitle === 1 ? "level" : "levels"} until ${level.nextTitle}.`)),
    h("div", { class: "streak-card" },
      h("span", { class: `streak-flame ${streak.activeToday ? "is-active" : ""}` }, icon("flame")),
      h("p", { class: "streak-number" }, String(streak.current)),
      h("p", { class: "career-sub" }, streak.current === 1 ? "day in a row" : "days in a row"),
      h("p", { class: "career-small" }, streak.activeToday
        ? "Today counts. Come back tomorrow!"
        : `Earn ${Math.max(0, streak.dailyGoal - streak.xpToday)} more XP today to keep it going.`),
      h("p", { class: "career-small" }, `Longest: ${streak.longest} · Streak freezes: ${streak.freezes}`)));
}

function reviewsBlock(reviews) {
  return h("section", { class: "career-section", "aria-labelledby": "reviews-title" },
    h("h2", { id: "reviews-title" }, "Reviews due today"),
    reviews.length
      ? h("ul", { class: "plain-list" }, reviews.map((r) => h("li", {},
          h("span", { class: "key" }, r.key), h("span", {}, r.title),
          h("a", { class: "button", href: `/review/${r.id}` }, icon("rotate-ccw"), "Review"))))
      : h("p", { class: "empty-line" }, "Nothing due. Closed tickets come back here a day later, then less and less often."));
}

function weakBlock(weak) {
  return h("section", { class: "career-section", "aria-labelledby": "weak-title" },
    h("h2", { id: "weak-title" }, "Worth another look"),
    weak.length
      ? h("ul", { class: "plain-list" }, weak.map((w) => h("li", {},
          h("span", { class: "key" }, w.key),
          h("span", {}, w.title, h("br"), h("span", { class: "career-small" }, w.reason,
            w.concepts.length ? ` Topics: ${w.concepts.join(", ")}.` : "")),
          h("a", { class: "button", href: `/t/${w.id}` }, "Open KB page"))))
      : h("p", { class: "empty-line" }, "No weak spots right now."));
}

function kudosBlock(kudos) {
  const earned = kudos.filter((k) => k.earned).length;
  return h("section", { class: "career-section", "aria-labelledby": "kudos-title" },
    h("h2", { id: "kudos-title" }, `Shabaash · ${earned} of ${kudos.length}`),
    h("ul", { class: "kudos-grid" }, kudos.map((k) => h("li", { class: `kudos ${k.earned ? "is-earned" : "is-locked"}` },
      k.earned ? avatar(k.from) : icon("lock", "Not earned yet"),
      h("div", {},
        h("p", { class: "kudos-title" }, k.title),
        h("p", { class: "career-small" }, k.earned ? `${TEAM[k.from].name.split(" ")[0]}: ${k.message}` : k.howToEarn))))));
}

function ticketsBlock(topics) {
  return h("section", { class: "career-section", "aria-labelledby": "tickets-title" },
    h("h2", { id: "tickets-title" }, "Ticket mastery"),
    h("table", { class: "mastery-table" },
      h("thead", {}, h("tr", {}, h("th", { scope: "col" }, "Ticket"), h("th", { scope: "col" }, "Title"), h("th", { scope: "col" }, "Mastery"))),
      h("tbody", {}, topics.map((t) => h("tr", {},
        h("td", { class: "key" }, t.key),
        h("td", {}, h("a", { href: `/t/${t.id}` }, t.title)),
        h("td", {}, h("span", { class: "mastery-bar", role: "img", "aria-label": `${t.masteryPercent}%` },
          h("span", { style: `width:${t.masteryPercent}%` })), ` ${t.masteryPercent}%`))))));
}

function backupBlock() {
  const fileInput = h("input", { type: "file", accept: "application/json", class: "visually-hidden", id: "import-file" });
  fileInput.addEventListener("change", async () => {
    const file = fileInput.files[0];
    if (!file) return;
    try {
      await api.importProgress(JSON.parse(await file.text()));
      toast("Progress restored from your backup.");
      location.reload();
    } catch (error) {
      toast(`Couldn't restore that file: ${error.message}`);
    }
  });
  const download = async () => {
    const data = await api.exportProgress();
    const url = URL.createObjectURL(new Blob([JSON.stringify(data, null, 2)], { type: "application/json" }));
    const link = h("a", { href: url, download: `cadence-progress-${new Date().toISOString().slice(0, 10)}.json` });
    document.body.append(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  };
  return h("section", { class: "career-section", "aria-labelledby": "backup-title" },
    h("h2", { id: "backup-title" }, "Backup"),
    h("p", { class: "career-small" }, "Your progress lives on this laptop only. Download a backup now and then. Restoring replaces your current progress."),
    h("div", { class: "button-row" },
      h("button", { type: "button", class: "button", onclick: download }, icon("save"), "Download backup"),
      h("label", { class: "button", for: "import-file" }, icon("rotate-ccw"), "Restore from backup"), fileInput));
}

export async function renderCareer(view) {
  const { startTourOnce } = await import("./tour.js");
  const summary = await refreshRewards() ?? await api.career();
  const offerReady = summary.kudos.some((k) => k.id === "java-pro" && k.earned);
  view.replaceChildren(h("div", { class: "career" },
    offerReady && h("a", { class: "offer-banner", href: "/offer" }, icon("star"),
      h("span", {}, h("strong", {}, "Your offer letter is ready."), " Spoke launched in Bramford, and Tsunade wrote you something."),
      icon("arrow-right")),
    levelBlock(summary),
    h("div", { class: "career-columns" }, reviewsBlock(summary.reviewsDue), weakBlock(summary.weak)),
    kudosBlock(summary.kudos),
    ticketsBlock(summary.topics),
    backupBlock()));
  startTourOnce("career");
  return () => {};
}
