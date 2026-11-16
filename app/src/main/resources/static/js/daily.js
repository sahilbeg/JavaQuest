import { api } from "./api.js";
import { h, toast } from "./dom.js";
import { bus } from "./events.js";

const MIN = 1;
const MAX = 20;
const SAVE_DELAY_MS = 500;

let saved = null;
let parts = null;
let timer = null;

/**
 * The daily limit control in the top bar, on screen on every page: a switch, tickets per day, and today's count.
 * Changes save at once. The page draws itself again ("settings" on the bus) when the course page shows the limit,
 * or when the change opens or closes the next ticket.
 */
export function mountDailyLimit(slot) {
  const toggle = h("input", { type: "checkbox", role: "switch", class: "switch", id: "daily-on" });
  const count = h("input", { type: "number", id: "daily-count", min: MIN, max: MAX, step: 1, inputmode: "numeric",
    "aria-label": "Tickets per day" });
  const minus = h("button", { type: "button", "aria-label": "One fewer ticket a day", onclick: () => step(-1) }, "−");
  const plus = h("button", { type: "button", "aria-label": "One more ticket a day", onclick: () => step(1) }, "+");
  const status = h("span", { class: "daily-status", id: "daily-status", "aria-live": "polite" });
  const root = h("div", { class: "daily", id: "daily-limit", "data-state": "loading", role: "group", "aria-labelledby": "daily-label",
      "aria-describedby": "daily-status" },
    toggle,
    h("label", { class: "daily-label", id: "daily-label", for: "daily-on" }, "Daily limit"),
    h("span", { class: "stepper stepper-sm" }, minus, count, plus),
    h("span", { class: "daily-unit", "aria-hidden": "true" }, "a day"),
    status);
  parts = { root, toggle, count, minus, plus, status };
  slot.replaceWith(root);

  toggle.addEventListener("change", () => save(0));
  count.addEventListener("change", () => save(0));
  count.addEventListener("keydown", (e) => {
    if (e.key === "Enter") save(0);
  });
  bus.addEventListener("career", () => refreshDailyLimit());
  refreshDailyLimit();
}

/** Reads the settings and today's count again, for example after a ticket is finished. */
export async function refreshDailyLimit() {
  if (!parts) return;
  try {
    render(await api.settings());
  } catch {
    // The connection banner already says the app is unreachable.
  }
}

/** Points the learner at the control: from the "Done for today" card and the "opens tomorrow" page. */
export function focusDailyLimit() {
  if (!parts) return;
  const target = parts.toggle.checked && !parts.count.disabled ? parts.count : parts.toggle;
  target.focus();
  parts.root.classList.remove("is-flash");
  void parts.root.offsetWidth;
  parts.root.classList.add("is-flash");
}

function step(by) {
  const value = Number.parseInt(parts.count.value, 10);
  parts.count.value = String(clamp((Number.isNaN(value) ? saved?.dailyLimit ?? 3 : value) + by));
  showStatus();
  save(SAVE_DELAY_MS);
}

const clamp = (n) => Math.min(MAX, Math.max(MIN, n));

function save(delay) {
  clearTimeout(timer);
  timer = setTimeout(async () => {
    const value = Number(parts.count.value);
    if (!Number.isInteger(value) || value < MIN || value > MAX) {
      toast(`Choose between ${MIN} and ${MAX} tickets a day.`);
      render(saved);
      return;
    }
    const before = saved;
    try {
      const after = await api.saveSettings({ dailyLimitOn: parts.toggle.checked, dailyLimit: value });
      render(after);
      if (before && (before.dailyLimitOn !== after.dailyLimitOn || before.dailyLimit !== after.dailyLimit)) {
        bus.dispatchEvent(new CustomEvent("settings", { detail: { reachedChanged: before.reached !== after.reached } }));
      }
    } catch (error) {
      toast(`Couldn't save the daily limit: ${error.message}`);
      render(saved);
    }
  }, delay);
}

function render(settings) {
  if (!settings) return;
  saved = settings;
  const { toggle, count } = parts;
  toggle.checked = settings.dailyLimitOn;
  toggle.disabled = settings.authorMode;
  if (document.activeElement !== count) count.value = String(settings.dailyLimit);
  showStatus();
}

/** Today's count against the number on screen, so the line updates while the learner clicks + and −. */
function showStatus() {
  const { root, toggle, count, minus, plus, status } = parts;
  const on = toggle.checked && !saved.authorMode;
  const limit = Number(count.value);
  const done = saved.doneToday;
  root.dataset.state = !on ? "off" : done >= limit ? "reached" : "on";
  for (const control of [count, minus, plus]) control.disabled = !on;
  minus.disabled ||= limit <= MIN;
  plus.disabled ||= limit >= MAX;
  status.textContent = saved.authorMode ? "Off in author mode"
    : !on ? `Off · ${done} done today`
    : done >= limit ? `Done today · ${done}/${limit}`
    : `${done}/${limit} today`;
  root.title = saved.authorMode ? "Author mode ignores the daily limit."
    : on ? `At most ${limit} new ${limit === 1 ? "ticket" : "tickets"} a day. Finished tickets and reviews stay open.`
    : "Off: study as many tickets as you like. Switch it on to pace yourself.";
}
