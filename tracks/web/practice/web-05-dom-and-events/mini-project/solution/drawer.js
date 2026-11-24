// The cart drawer. A line looks like { id: "DC-1042", qty: 2 }; a product like { id, name, paise }.
import { rupees } from "./money.js";

/** How many pieces are in the cart: the sum of every qty. */
export function countItems(lines) {
  return lines.reduce((sum, line) => sum + line.qty, 0);
}

/** The total in paise: each line's product price times its qty, added up. */
export function cartTotal(lines, catalogue) {
  return lines.reduce((sum, line) => {
    const product = catalogue.find((item) => item.id === line.id);
    return sum + product.paise * line.qty;
  }, 0);
}

/** A new array with one line's qty changed by delta. New ids are added at the end; lines at 0 are removed. */
export function changeQty(lines, id, delta) {
  if (!lines.some((line) => line.id === id)) {
    return delta > 0 ? [...lines, { id, qty: delta }] : lines;
  }
  return lines
    .map((line) => (line.id === id ? { ...line, qty: line.qty + delta } : line))
    .filter((line) => line.qty > 0);
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

  function part(tag, className, text) {
    const element = document.createElement(tag);
    element.className = className;
    element.textContent = text;
    return element;
  }

  function actionButton(action, text, label) {
    const button = document.createElement("button");
    button.type = "button";
    button.dataset.action = action;
    button.textContent = text;
    button.setAttribute("aria-label", label);
    return button;
  }

  function lineItem(line) {
    const product = productOf(line.id);
    const li = document.createElement("li");
    li.className = "line";
    li.dataset.id = line.id;
    li.append(
      part("span", "name", product.name),
      actionButton("dec", "−", `One less ${product.name}`),
      part("span", "qty", String(line.qty)),
      actionButton("inc", "+", `One more ${product.name}`),
      part("span", "amount", rupees(product.paise * line.qty)),
    );
    return li;
  }

  function render() {
    toggle.textContent = `Cart (${countItems(state.lines)})`;
    toggle.setAttribute("aria-expanded", String(state.open));
    drawer.hidden = !state.open;
    list.replaceChildren(...state.lines.map(lineItem));
    empty.hidden = state.lines.length > 0;
    total.textContent = rupees(cartTotal(state.lines, catalogue));
  }

  function update(changes) {
    state = { ...state, ...changes };
    render();
  }

  function openDrawer() {
    update({ open: true });
    close.focus();
  }

  function closeDrawer() {
    update({ open: false });
    toggle.focus();
  }

  toggle.addEventListener("click", () => {
    if (state.open) {
      closeDrawer();
    } else {
      openDrawer();
    }
  });

  close.addEventListener("click", closeDrawer);

  drawer.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      closeDrawer();
    }
  });

  products.addEventListener("click", (event) => {
    const button = event.target.closest(".add");
    if (button === null) {
      return;
    }
    update({ lines: changeQty(state.lines, button.closest(".product").dataset.id, 1) });
  });

  list.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-action]");
    if (button === null) {
      return;
    }
    const delta = button.dataset.action === "inc" ? 1 : -1;
    update({ lines: changeQty(state.lines, button.closest(".line").dataset.id, delta) });
  });

  render();
  return { getLines: () => state.lines };
}
