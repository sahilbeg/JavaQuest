// The sale page's cart. A line looks like { id: "DC-1042", qty: 2 }.
import { rupees } from "./money.js";

export const CART_KEY = "dhaaga:cart";

/** One more of this item: 1 more on its line, or a new line. Returns a new array. */
export function addOne(lines, id) {
  if (lines.some((line) => line.id === id)) {
    return lines.map((line) => (line.id === id ? { ...line, qty: line.qty + 1 } : line));
  }
  return [...lines, { id, qty: 1 }];
}

/** Saves the cart in this browser: every line, with its quantity. */
export function saveCart(lines) {
  localStorage.setItem(CART_KEY, JSON.stringify(lines));
}

function isLine(value) {
  return typeof value === "object" && value !== null
    && typeof value.id === "string"
    && Number.isInteger(value.qty) && value.qty > 0;
}

/** The cart saved in this browser, as [{ id, qty }]: [] when nothing is saved or the saved text is broken. */
export function loadCart() {
  const saved = localStorage.getItem(CART_KEY);
  if (saved === null) {
    return [];
  }
  try {
    const data = JSON.parse(saved);
    return Array.isArray(data) ? data.filter(isLine) : [];
  } catch {
    return [];
  }
}

/** Wires the Add buttons inside root and draws the cart. */
export function createCart(root, catalogue) {
  const products = root.querySelector("#products");
  const list = root.querySelector("#cart-lines");
  const empty = root.querySelector("#cart-empty");
  const total = root.querySelector("#cart-total");

  function productOf(line) {
    return catalogue.find((product) => product.id === line.id);
  }

  let lines = loadCart().filter((line) => productOf(line) !== undefined);

  function lineItem(line) {
    const product = productOf(line);
    const li = document.createElement("li");
    li.className = "line";
    li.dataset.id = line.id;

    const name = document.createElement("span");
    name.className = "name";
    name.textContent = product.name;

    const qty = document.createElement("span");
    qty.className = "qty";
    qty.textContent = String(line.qty);

    const amount = document.createElement("span");
    amount.className = "amount";
    amount.textContent = rupees(product.paise * line.qty);

    li.append(name, " × ", qty, " ", amount);
    return li;
  }

  function render() {
    list.replaceChildren(...lines.map(lineItem));
    empty.hidden = lines.length > 0;
    total.textContent = rupees(lines.reduce((sum, line) => sum + productOf(line).paise * line.qty, 0));
  }

  products.addEventListener("click", (event) => {
    const button = event.target.closest(".add");
    if (button === null) {
      return;
    }
    lines = addOne(lines, button.closest(".product").dataset.id);
    saveCart(lines);
    render();
  });

  render();
}
