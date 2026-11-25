// Checkout as a single-page app: four steps, each with its own address, /checkout/cart to /checkout/done.
// The store's state looks like:
// { lines: [{ id: "DC-1042", qty: 1 }], details: { name: "", phone: "", pin: "" }, paid: false }
import { STEP_VIEWS, STEP_TITLES } from "./steps.js";

export const PRICES = { "DC-1042": 45000, "DC-2001": 15000, "DC-3001": 89900 };
export const STEPS = ["cart", "details", "pay", "done"];

/** The cart's price in paise, before delivery. */
export function subtotal(lines) {
  // TODO 1: add up PRICES[line.id] * line.qty for every line.
  return 0;
}

/** Delivery inside Pune: 0 for an empty cart, ₹60 (6000 paise) up to ₹999, free above ₹999. */
export function deliveryFee(subtotalPaise) {
  // TODO 2: 0 when the subtotal is 0 or above 99900, and 6000 otherwise.
  return 0;
}

/** What the customer pays, in paise. */
export function orderTotal(lines) {
  // TODO 3: the subtotal plus the delivery fee for that subtotal.
  return 0;
}

/** The details form's problems, as { field: message }, or {} when all is well. */
export function detailsErrors(details) {
  const errors = {};
  // TODO 4: an empty name (after trim) gives errors.name = "Please tell us your name.".
  //         A phone not matching /^\d{5} ?\d{5}$/ gives errors.phone = "A phone number has 10 digits, like 98765 43210.".
  //         A PIN not matching /^[1-9]\d{5}$/ gives errors.pin = "A PIN code has 6 digits, like 411004.". Trim before testing.
  return errors;
}

/** The furthest step the customer may reach now. */
export function furthestStep(state) {
  // TODO 5: "done" once paid; "cart" while the cart is empty; "details" while detailsErrors has any errors; else "pay".
  return "cart";
}

/** Whether the customer may open this step now. After paying, only "done" is open. */
export function canVisit(state, step) {
  // TODO 6: false for anything not in STEPS. Once paid, only "done". Otherwise, any step up to the furthest one
  //         (compare positions with STEPS.indexOf).
  return false;
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
    // TODO 7: when canVisit(state, step) is false, use furthestStep(state) instead, and swap the address to
    //         `/checkout/${step}` with history.replaceState, so Back never returns to the blocked step.
    const context = { state, store, go, total: orderTotal(state.lines), errors: detailsErrors(state.details) };
    // TODO 8: draw STEP_VIEWS[step](context) in the outlet with replaceChildren,
    //         and set document.title to `Checkout: ${STEP_TITLES[step]} | Dhaaga & Co.`.
  }

  function go(step) {
    // TODO 9: push `/checkout/${step}` with history.pushState, unless the address is already that path. Then show().
  }

  function start() {
    // TODO 10: show() once now, and again on every popstate on window.
  }

  function stop() {
    // TODO 11: remove the popstate listener.
  }

  return { start, stop, go };
}
