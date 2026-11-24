import { h, icon } from "./dom.js";
import { bus } from "./events.js";

// The career ladder down the left edge of every page: every job title from Intern (bottom) to Principal
// Engineer (top), with the learner's place on it. It redraws each time the career summary changes.
// Hovering any part shows a short note on what it measures, so the line and the bar never look like they disagree.

const n = (value) => value.toLocaleString();
const pct = (fraction) => `${Math.round(fraction * 100)}%`;

/** Draws the ladder into the rail, keeps it current, and explains each part on hover. */
export function mountLadder(rail) {
  bus.addEventListener("career", (event) => render(rail, event.detail.level));
  mountTips(rail);
}

function render(rail, level) {
  const ladder = level?.ladder ?? [];
  if (!ladder.length) {
    rail.hidden = true;
    return;
  }
  const current = Math.max(0, ladder.findLastIndex((rung) => level.level >= rung.firstLevel));
  const rung = ladder[current];
  const next = ladder[current + 1];
  const span = level.nextLevelXp - level.levelStartXp;
  const intoLevelXp = Math.max(0, level.xp - level.levelStartXp);
  const intoLevel = span > 0 ? Math.min(1, intoLevelXp / span) : 0;
  const levelsInTitle = next ? next.firstLevel - rung.firstLevel : 1;
  const levelsDone = level.level - rung.firstLevel + intoLevel;
  const intoTitle = Math.min(1, levelsDone / levelsInTitle);
  // The fill runs from Intern's dot up to the current dot, then part of the way to the next one.
  const fill = ladder.length > 1 ? (current + (next ? intoTitle : 0)) / (ladder.length - 1) : 1;
  const toNextLevel = Math.max(0, level.nextLevelXp - level.xp);
  const rangeOf = (i) => (ladder[i + 1] ? `levels ${ladder[i].firstLevel}–${ladder[i + 1].firstLevel - 1}` : `level ${ladder[i].firstLevel} and up`);

  const tips = {
    line: next
      ? `The line shows your progress through ${rung.title}, ${rangeOf(current)}. You're ${levelsDone.toFixed(1)} of ${levelsInTitle} levels in (${pct(intoTitle)}), so it's ${pct(intoTitle)} of the way to ${next.title}.`
      : `${rung.title} is the top title, so the line is full.`,
    bar: `The bar shows level ${level.level} only: ${n(intoLevelXp)} of ${n(span)} XP (${pct(intoLevel)}). ${n(toNextLevel)} XP more reaches level ${level.level + 1}.`,
    head: `${level.courseTitle ?? "Your course"}: your level and the XP you've earned in it. Each course has its own ladder.`,
  };
  const rungTip = (r, i, state) => {
    if (state === "done") return `${r.title}: ${rangeOf(i)}. Done.`;
    if (state === "current") return `You're level ${level.level}, a ${r.title}. This title covers ${rangeOf(i)}.`;
    const away = r.xpToReach - level.xp;
    return i === current + 1
      ? `${r.title} starts at level ${r.firstLevel}, which takes ${n(r.xpToReach)} XP. You have ${n(level.xp)}, so ${n(away)} to go.`
      : `${r.title} starts at level ${r.firstLevel}, at ${n(r.xpToReach)} XP. That's ${n(away)} XP from where you are.`;
  };

  rail.hidden = false;
  rail.style.setProperty("--rungs", ladder.length);
  rail.replaceChildren(
    h("div", { class: "ladder-head", "data-tip": tips.head },
      h("span", { class: "ladder-head-mark", "aria-hidden": "true" }, icon("star")),
      h("div", { class: "ladder-text" },
        h("p", { class: "eyebrow" }, "Career ladder"),
        h("p", { class: "ladder-now" }, `Level ${level.level} · ${n(level.xp)} XP`))),
    h("div", { class: "ladder-body" },
      h("span", { class: "ladder-track", "aria-hidden": "true", "data-tip": tips.line },
        h("span", { class: "ladder-fill", style: `height:${(fill * 100).toFixed(2)}%` })),
      h("ol", { class: "ladder-list" }, ladder.map((r, i) => {
        const state = i < current ? "done" : i === current ? "current" : "ahead";
        const range = ladder[i + 1] ? `Lv ${r.firstLevel}–${ladder[i + 1].firstLevel - 1}` : `Lv ${r.firstLevel}+`;
        return h("li", { class: `rung rung-${state}`, "aria-current": state === "current" ? "step" : null, "data-tip": rungTip(r, i, state) },
          h("span", { class: "rung-dot", "aria-hidden": "true" },
            state === "done" ? icon("check") : state === "current" ? String(level.level) : null),
          h("span", { class: "ladder-text rung-text" },
            h("span", { class: "rung-title" }, r.title),
            state === "current"
              ? [h("span", { class: "rung-meta" }, `${n(toNextLevel)} XP to level ${level.level + 1}`),
                  h("span", { class: "rung-bar", "aria-hidden": "true", "data-tip": tips.bar },
                    h("span", { class: "rung-bar-fill", style: `width:${Math.round(intoLevel * 100)}%` }))]
              : h("span", { class: "rung-meta" }, state === "ahead" && i === current + 1
                  ? `Lv ${r.firstLevel} · ${n(r.xpToReach - level.xp)} XP away`
                  : range),
            // Screen readers get the same explanations the tooltips give.
            h("span", { class: "visually-hidden" }, state === "current" ? `. ${tips.bar} ${tips.line}` : `. ${rungTip(r, i, state)}`)));
      }))),
    h("div", { class: "ladder-foot ladder-text" },
      h("a", { href: "/career" }, "Career page", icon("arrow-right")),
      h("a", { href: "/start" }, "Switch course")));
  rail.setAttribute("aria-label", `Career ladder: ${rung.title}, level ${level.level}`);
}

/** One tooltip for the whole rail. It follows the pointer down the right edge of the rail, so it never covers it. */
function mountTips(rail) {
  const tip = h("div", { class: "ladder-tip", role: "tooltip", id: "ladder-tip", hidden: true });
  document.body.append(tip);
  const hide = () => { tip.hidden = true; };
  rail.addEventListener("pointermove", (event) => {
    const target = event.target.closest?.("[data-tip]");
    if (!target || !rail.contains(target)) return hide();
    tip.textContent = target.dataset.tip;
    tip.hidden = false;
    const edge = rail.getBoundingClientRect().right;
    const top = Math.min(event.clientY - tip.offsetHeight / 2, innerHeight - tip.offsetHeight - 8);
    tip.style.left = `${edge + 10}px`;
    tip.style.top = `${Math.max(8, top)}px`;
  });
  rail.addEventListener("pointerleave", hide);
  addEventListener("scroll", hide, { passive: true });
}
