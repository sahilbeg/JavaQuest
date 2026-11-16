import { api } from "./api.js";
import { h, icon, toast, reducedMotion } from "./dom.js";
import { byte } from "./byte.js";
import { bus } from "./events.js";
import { glow, xpFloat } from "./celebrate.js";

let last;

/** Reads the career summary, updates the top bar, and celebrates anything new since the last read. */
export async function refreshRewards() {
  let summary;
  try {
    summary = await api.career();
  } catch {
    return null;
  }
  renderTopbar(summary);
  if (last) celebrate(last, summary);
  last = summary;
  bus.dispatchEvent(new CustomEvent("career", { detail: summary }));
  return summary;
}

function renderTopbar(summary) {
  const { level, streak } = summary;
  const span = level.nextLevelXp - level.levelStartXp;
  const into = level.xp - level.levelStartXp;
  const slot = document.getElementById("career-chip");
  slot.replaceChildren(
    h("a", { class: "career-chip", href: "#/career", "aria-label": `${level.title}, level ${level.level}, ${level.xp} XP, ${streak.current} day streak. Open your career page.` },
      h("span", { class: "stat stat-level", "aria-hidden": "true" }, icon("star"), `Lv ${level.level}`,
        h("span", { class: "stat-title" }, level.title),
        h("span", { class: "xp-bar" }, h("span", { class: "xp-bar-fill", style: `width:${Math.round((into / span) * 100)}%` }))),
      h("span", { class: `stat stat-streak ${streak.activeToday ? "is-active" : ""}`, "aria-hidden": "true" }, icon("flame"), String(streak.current))));
}

function celebrate(before, after) {
  const gained = after.level.xp - before.level.xp;
  if (gained > 0) {
    xpFloat(gained);
    toast(`+${gained} XP`);
  }
  if (after.level.title !== before.level.title) {
    promotion(after.level);
  } else if (after.level.level > before.level.level) {
    glow();
    toast(`Level ${after.level.level}! Keep going.`);
  }
  if (after.streak.activeToday && !before.streak.activeToday) {
    toast(`Day ${after.streak.current} of your streak. Nice!`);
  }
  const had = new Set(before.kudos.filter((k) => k.earned).map((k) => k.id));
  for (const kudos of after.kudos.filter((k) => k.earned && !had.has(k.id))) {
    toast(`Kudos from ${kudos.from[0].toUpperCase()}${kudos.from.slice(1)}: ${kudos.title}. ${kudos.message}`);
  }
}

function promotion(level) {
  const dialog = h("dialog", { class: "promotion", "aria-labelledby": "promotion-title" },
    byte("idle", "", { size: "lg" }),
    h("h2", { id: "promotion-title" }, `You're now a ${level.title}!`),
    h("p", {}, `Maya: "Your work has been great. Congratulations on the promotion. On to the next ticket!"`),
    h("form", { method: "dialog" }, h("button", { class: "button button-primary button-big", autofocus: true }, "Thanks, Maya")));
  if (!reducedMotion()) dialog.classList.add("promotion-animate");
  document.body.append(dialog);
  dialog.addEventListener("close", () => dialog.remove());
  dialog.showModal();
  glow();
}
