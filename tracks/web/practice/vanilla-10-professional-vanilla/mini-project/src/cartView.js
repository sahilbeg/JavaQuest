import { el } from "./dom.js";
import { rupees } from "./money.js";
import { cartTotals } from "./pricing.js";
import { MAX_QTY } from "./cart.js";

/** Draws the cart into section and keeps it in step with the store. Returns a function that stops following the store. */
export function mountCart(section, store, onCheckout) {
  const heading = el("h2", { id: "cart-heading", tabindex: "-1" }, "Your cart");
  const list = el("ul", { id: "cart-lines" });
  const empty = el("p", { id: "cart-empty" }, "Your cart is empty.");
  const totals = el("div", { id: "cart-totals" });
  const checkout = el("button", { id: "checkout", type: "button" }, "Checkout");
  section.replaceChildren(heading, list, empty, totals, checkout);

  function lineItem(line) {
    const selectId = `qty-${line.id}`;
    const options = Array.from({ length: MAX_QTY }, (_, index) => el("option", { value: index + 1 }, String(index + 1)));
    const select = el("select", { id: selectId, class: "qty" }, ...options);
    select.value = String(line.qty);
    // TODO 6: tie the label to its select (for: selectId), and give the Remove button
    //         aria-label "Remove <line name>", so each control has its own name
    return el(
      "li",
      { class: "line", "data-id": line.id },
      el("span", { class: "name" }, line.name),
      el("label", { class: "visually-hidden" }, `Quantity for ${line.name}`),
      select,
      el("span", { class: "amount" }, rupees(line.paise * line.qty)),
      el("button", { type: "button", class: "remove" }, "Remove"),
    );
  }

  function render(lines) {
    list.replaceChildren(...lines.map(lineItem));
    // TODO 7: draw the rest from lines:
    //   - empty.hidden unless the cart is empty
    //   - totals: one <p> per row from cartTotals(lines): "Subtotal: ₹450.00", then "Diwali offer: -₹180.00" only
    //     when there is a discount, then "Delivery: ₹60.00" or "Delivery: Free", then "Total: ₹510.00";
    //     hide totals when the cart is empty
    //   - checkout.disabled when the cart is empty
    //   - keep focus: remember the id of the focused element in section before redrawing, and focus the
    //     element with that id again afterwards (so a quantity select keeps focus after it changes)
  }

  // TODO 8: one "change" listener on list: a select.qty dispatches { type: "setQty", id, qty } with qty as a number.
  //         One "click" listener on list: a button.remove dispatches { type: "remove", id }, then moves focus to heading.
  //         A click on checkout calls onCheckout().

  const unsubscribe = store.subscribe(render);
  render(store.getState());
  return unsubscribe;
}
