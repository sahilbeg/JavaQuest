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
    return el(
      "li",
      { class: "line", "data-id": line.id },
      el("span", { class: "name" }, line.name),
      el("label", { for: selectId, class: "visually-hidden" }, `Quantity for ${line.name}`),
      select,
      el("span", { class: "amount" }, rupees(line.paise * line.qty)),
      el("button", { type: "button", class: "remove", "aria-label": `Remove ${line.name}` }, "Remove"),
    );
  }

  function render(lines) {
    const focusedId = section.contains(document.activeElement) ? document.activeElement.id : "";
    list.replaceChildren(...lines.map(lineItem));
    empty.hidden = lines.length > 0;

    const { subtotal, discount, delivery, total } = cartTotals(lines);
    const rows = [`Subtotal: ${rupees(subtotal)}`];
    if (discount > 0) {
      rows.push(`Diwali offer: -${rupees(discount)}`);
    }
    rows.push(`Delivery: ${delivery === 0 ? "Free" : rupees(delivery)}`);
    rows.push(`Total: ${rupees(total)}`);
    totals.replaceChildren(...rows.map((text) => el("p", {}, text)));
    totals.hidden = lines.length === 0;
    checkout.disabled = lines.length === 0;

    if (focusedId !== "" && !section.contains(document.activeElement)) {
      document.getElementById(focusedId)?.focus();
    }
  }

  list.addEventListener("change", (event) => {
    const select = event.target.closest("select.qty");
    if (select === null) {
      return;
    }
    store.dispatch({ type: "setQty", id: select.closest(".line").dataset.id, qty: Number(select.value) });
  });

  list.addEventListener("click", (event) => {
    const button = event.target.closest("button.remove");
    if (button === null) {
      return;
    }
    store.dispatch({ type: "remove", id: button.closest(".line").dataset.id });
    heading.focus();
  });

  checkout.addEventListener("click", () => onCheckout());

  const unsubscribe = store.subscribe(render);
  render(store.getState());
  return unsubscribe;
}
