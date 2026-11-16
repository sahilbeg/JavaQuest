import { api } from "./api.js";
import { h, icon, toast, reducedMotion } from "./dom.js";
import { bus } from "./events.js";

const MIN = 1;
const MAX = 20;
const tickets = (n) => `${n} ${n === 1 ? "ticket" : "tickets"}`;

/** Opens the settings dialog. Saving tells the page to draw itself again ("settings" on the bus). */
export async function openSettings() {
  if (document.querySelector(".settings-dialog")) return;
  let settings;
  let catalog;
  try {
    [settings, catalog] = await Promise.all([api.settings(), api.catalog()]);
  } catch (error) {
    toast(`Couldn't open the settings: ${error.message}`);
    return;
  }
  const done = catalog.today.doneToday;

  const toggle = h("input", { type: "checkbox", role: "switch", id: "daily-limit-on", class: "switch",
    "aria-describedby": "daily-limit-help", checked: settings.dailyLimitOn });
  const count = h("input", { type: "number", id: "daily-limit", min: MIN, max: MAX, step: 1, inputmode: "numeric",
    value: settings.dailyLimit, "aria-describedby": "daily-limit-status" });
  const status = h("p", { class: "setting-status", id: "daily-limit-status", "aria-live": "polite" });
  const error = h("p", { class: "field-error", role: "alert", hidden: true });
  const step = (by) => {
    const value = Number.parseInt(count.value, 10);
    count.value = String(Math.min(MAX, Math.max(MIN, (Number.isNaN(value) ? settings.dailyLimit : value) + by)));
    update();
  };
  const minus = h("button", { type: "button", "aria-label": "One fewer ticket a day", onclick: () => step(-1) }, "−");
  const plus = h("button", { type: "button", "aria-label": "One more ticket a day", onclick: () => step(1) }, "+");
  const amount = h("fieldset", { class: "setting-sub" },
    h("legend", { class: "visually-hidden" }, "Daily limit"),
    h("div", { class: "setting-row" },
      h("label", { for: "daily-limit" }, "Tickets per day"),
      h("span", { class: "stepper" }, minus, count, plus)),
    status);

  function valid() {
    const value = Number(count.value);
    return Number.isInteger(value) && value >= MIN && value <= MAX ? value : null;
  }

  function update() {
    amount.disabled = !toggle.checked;
    error.hidden = true;
    const limit = valid();
    const today = `You've completed ${tickets(done)} today.`;
    if (!toggle.checked) {
      status.textContent = `${today} With the limit off, every ticket opens as soon as the one before it is done.`;
    } else if (limit == null) {
      status.textContent = `Choose a number from ${MIN} to ${MAX}.`;
    } else if (done >= limit) {
      status.textContent = `${today} The next new ticket opens tomorrow.`;
    } else {
      status.textContent = `${today} ${tickets(limit - done)} left for today.`;
    }
  }

  const save = h("button", { type: "submit", class: "button button-primary" }, "Save");
  const form = h("form", { class: "settings-form" },
    h("header", { class: "settings-head" },
      h("h2", { id: "settings-title" }, "Settings"),
      h("button", { type: "button", class: "icon-button", "aria-label": "Close", onclick: () => dialog.close() }, icon("x"))),
    h("div", { class: "settings-body" },
      h("section", { class: "setting", "aria-labelledby": "daily-limit-title" },
        h("div", { class: "setting-row" },
          h("label", { class: "setting-label", for: "daily-limit-on" },
            h("strong", { id: "daily-limit-title" }, "Daily limit"),
            h("span", { class: "setting-help", id: "daily-limit-help" },
              "Learn a few tickets a day and come back tomorrow for the rest. Spacing it out helps it stick. Turn it off to study as much as you like.")),
          toggle),
        amount,
        h("p", { class: "setting-help" }, "Tickets you've already finished, and your reviews, stay open either way."),
        catalog.authorMode && h("p", { class: "setting-help setting-note" }, "Author mode ignores the daily limit, so every ticket stays open here.")),
      error),
    h("footer", { class: "settings-foot" },
      h("button", { type: "button", class: "button button-quiet", onclick: () => dialog.close() }, "Cancel"),
      save));

  const dialog = h("dialog", { class: "settings-dialog", "aria-labelledby": "settings-title" }, form);
  if (!reducedMotion()) dialog.classList.add("promotion-animate");

  toggle.addEventListener("change", update);
  count.addEventListener("input", update);
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const limit = valid();
    if (limit == null) {
      error.textContent = `Choose between ${MIN} and ${MAX} tickets a day.`;
      error.hidden = false;
      count.focus();
      return;
    }
    save.setAttribute("aria-busy", "true");
    save.disabled = true;
    try {
      const saved = await api.saveSettings({ dailyLimitOn: toggle.checked, dailyLimit: limit });
      dialog.close();
      toast(saved.dailyLimitOn ? `Daily limit on: ${tickets(saved.dailyLimit)} a day.` : "Daily limit off. Study as much as you like.");
      bus.dispatchEvent(new Event("settings"));
    } catch (failure) {
      error.textContent = failure.message;
      error.hidden = false;
    } finally {
      save.removeAttribute("aria-busy");
      save.disabled = false;
    }
  });

  update();
  document.body.append(dialog);
  dialog.addEventListener("close", () => {
    dialog.remove();
    document.getElementById("settings-button")?.focus();
  });
  dialog.showModal();
  toggle.focus();
}
