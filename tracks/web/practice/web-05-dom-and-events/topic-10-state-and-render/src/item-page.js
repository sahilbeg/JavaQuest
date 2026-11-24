// Golu's item page: one state object, and one render function that draws the page from it.
import { rupees } from "./money.js";

export const GIFT_WRAP_PAISE = 3000;
export const STITCH_PAISE = 5000;

/** The total in paise: (price, plus stitching when stitch isn't blank) times qty, plus gift wrap once. */
export function itemTotal(item, state) {
  // TODO 1: work out the price per piece, multiply by state.qty, then add GIFT_WRAP_PAISE once if state.giftWrap
  return 0;
}

/** Wires the page inside root. Returns { getState } so the current state can be read. */
export function createItemPage(root, item, max = 5) {
  let state = { qty: 1, giftWrap: false, stitch: "" };

  const qtyText = root.querySelector(".qty");
  const minus = root.querySelector('[data-action="dec"]');
  const plus = root.querySelector('[data-action="inc"]');
  const wrap = root.querySelector("#gift-wrap");
  const stitch = root.querySelector("#stitch");
  const summary = root.querySelector("#summary");
  const total = root.querySelector("#total");

  function render() {
    // TODO 2: from state only: qtyText, minus.disabled (at 1), plus.disabled (at max), wrap.checked, summary "2 × Golu the elephant" and total in rupees
  }

  function update(changes) {
    // TODO 3: state = a new object with the old fields and the changes on top; then render()
  }

  // TODO 4: one click listener on root: "inc" adds 1 below max, "dec" takes 1 away above 1, both through update
  // TODO 5: wrap's change event updates giftWrap with wrap.checked; stitch's input event updates stitch with stitch.value

  render();
  return { getState: () => state };
}
