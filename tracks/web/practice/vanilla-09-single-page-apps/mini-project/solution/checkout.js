// Checkout as a single-page app: four steps, each with its own address, /checkout/cart to /checkout/done.
// The store's state looks like:
// { lines: [{ id: "DC-1042", qty: 1 }], details: { name: "", phone: "", pin: "" }, paid: false }
import { STEP_VIEWS, STEP_TITLES } from "./steps.js";

export const PRICES = { "DC-1042": 45000, "DC-2001": 15000, "DC-3001": 89900 };
export const STEPS = ["cart", "details", "pay", "done"];

/** The cart's price in paise, before delivery. */
export function subtotal(lines) {
  return lines.reduce((sum, line) => sum + PRICES[line.id] * line.qty, 0);
}

/** Delivery inside Pune: 0 for an empty cart, ₹60 (6000 paise) up to ₹999, free above ₹999. */
export function deliveryFee(subtotalPaise) {
  return subtotalPaise === 0 || subtotalPaise > 99900 ? 0 : 6000;
}

/** What the customer pays, in paise. */
export function orderTotal(lines) {
  const sub = subtotal(lines);
  return sub + deliveryFee(sub);
}

/** The details form's problems, as { field: message }, or {} when all is well. */
export function detailsErrors(details) {
  const errors = {};
  if (details.name.trim() === "") {
    errors.name = "Please tell us your name.";
  }
  if (!/^\d{5} ?\d{5}$/.test(details.phone.trim())) {
    errors.phone = "A phone number has 10 digits, like 98765 43210.";
  }
  if (!/^[1-9]\d{5}$/.test(details.pin.trim())) {
    errors.pin = "A PIN code has 6 digits, like 411004.";
  }
  return errors;
}

/** The furthest step the customer may reach now. */
export function furthestStep(state) {
  if (state.paid) {
    return "done";
  }
  if (state.lines.length === 0) {
    return "cart";
  }
  if (Object.keys(detailsErrors(state.details)).length > 0) {
    return "details";
  }
  return "pay";
}

/** Whether the customer may open this step now. After paying, only "done" is open. */
export function canVisit(state, step) {
  if (!STEPS.includes(step)) {
    return false;
  }
  if (state.paid) {
    return step === "done";
  }
  return STEPS.indexOf(step) <= STEPS.indexOf(furthestStep(state));
}

/** The checkout's router. Returns { start, stop, go }. */
export function createCheckout(outlet, store) {
  function stepInAddress() {
    const parts = location.pathname.split("/");
    return parts[1] === "checkout" ? parts[2] : undefined;
  }

  function show() {
    const state = store.getState();
    let step = stepInAddress();
    if (!canVisit(state, step)) {
      step = furthestStep(state);
      history.replaceState(null, "", `/checkout/${step}`);
    }
    const context = { state, store, go, total: orderTotal(state.lines), errors: detailsErrors(state.details) };
    outlet.replaceChildren(STEP_VIEWS[step](context));
    document.title = `Checkout: ${STEP_TITLES[step]} | Dhaaga & Co.`;
  }

  function go(step) {
    const path = `/checkout/${step}`;
    if (path !== location.pathname) {
      history.pushState(null, "", path);
    }
    show();
  }

  function start() {
    show();
    window.addEventListener("popstate", show);
  }

  function stop() {
    window.removeEventListener("popstate", show);
  }

  return { start, stop, go };
}
