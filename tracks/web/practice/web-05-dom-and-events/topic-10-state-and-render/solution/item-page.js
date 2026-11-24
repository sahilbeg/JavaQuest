// Golu's item page: one state object, and one render function that draws the page from it.
import { rupees } from "./money.js";

export const GIFT_WRAP_PAISE = 3000;
export const STITCH_PAISE = 5000;

/** The total in paise: (price, plus stitching when stitch isn't blank) times qty, plus gift wrap once. */
export function itemTotal(item, state) {
  const perPiece = item.paise + (state.stitch.trim() === "" ? 0 : STITCH_PAISE);
  return perPiece * state.qty + (state.giftWrap ? GIFT_WRAP_PAISE : 0);
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
    qtyText.textContent = String(state.qty);
    minus.disabled = state.qty <= 1;
    plus.disabled = state.qty >= max;
    wrap.checked = state.giftWrap;
    summary.textContent = `${state.qty} × ${item.name}`;
    total.textContent = rupees(itemTotal(item, state));
  }

  function update(changes) {
    state = { ...state, ...changes };
    render();
  }

  root.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-action]");
    if (button === null) {
      return;
    }
    if (button.dataset.action === "inc" && state.qty < max) {
      update({ qty: state.qty + 1 });
    }
    if (button.dataset.action === "dec" && state.qty > 1) {
      update({ qty: state.qty - 1 });
    }
  });

  wrap.addEventListener("change", () => update({ giftWrap: wrap.checked }));
  stitch.addEventListener("input", () => update({ stitch: stitch.value }));

  render();
  return { getState: () => state };
}
