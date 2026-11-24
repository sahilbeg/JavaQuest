// The cart drawer. A line looks like { id: "DC-1042", qty: 2 }; a product like { id, name, paise }.
import { rupees } from "./money.js";

/** How many pieces are in the cart: the sum of every qty. */
export function countItems(lines) {
  // TODO 1: add up every line's qty (reduce works well)
  return 0;
}

/** The total in paise: each line's product price times its qty, added up. */
export function cartTotal(lines, catalogue) {
  // TODO 2: for each line, find its product in the catalogue and add product.paise * line.qty
  return 0;
}

/** A new array with one line's qty changed by delta. New ids are added at the end; lines at 0 are removed. */
export function changeQty(lines, id, delta) {
  // TODO 3: if no line has this id, add { id, qty: delta } when delta > 0, else return the lines unchanged;
  //         otherwise map to change that line's qty, then filter out lines whose qty is 0
  return lines;
}

/** Wires the cart button, the drawer and the Add buttons inside root. Returns { getLines }. */
export function createDrawer(root, catalogue, startLines = []) {
  const toggle = root.querySelector("#cart-toggle");
  const drawer = root.querySelector("#cart-drawer");
  const close = root.querySelector("#cart-close");
  const products = root.querySelector("#products");
  const list = root.querySelector("#drawer-lines");
  const empty = root.querySelector("#drawer-empty");
  const total = root.querySelector("#drawer-total");

  let state = { open: false, lines: startLines };

  function productOf(id) {
    return catalogue.find((product) => product.id === id);
  }

  function render() {
    // TODO 4: draw everything from state: toggle text "Cart (N)" and aria-expanded, drawer.hidden,
    //         one li.line per line (name, dec button, qty, inc button, amount), empty.hidden and the total
  }

  function update(changes) {
    state = { ...state, ...changes };
    render();
  }

  // TODO 5: a click on toggle opens the drawer (then close.focus()) or closes it (then toggle.focus())
  // TODO 6: a click on close, or an Escape keydown inside the drawer, closes it and calls toggle.focus()
  // TODO 7: one click listener on products: an .add click adds one of its card's data-id with changeQty
  // TODO 8: one click listener on list: data-action "inc" adds one, "dec" takes one away, for the line's data-id

  render();
  return { getLines: () => state.lines };
}
