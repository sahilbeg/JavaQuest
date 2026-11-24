// Shared helpers for the incident checks: open the sale page, click Add, read the cart.
import { createCart } from "../src/cart.js";

export const CATALOGUE = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1001", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-1013", name: "Rani pink keychain", paise: 12000 },
];

const PAGE = `
  <main id="app">
    <ul id="products">
      <li class="product" data-id="DC-1042"><h3>Golu the elephant</h3><button class="add" type="button">Add to cart</button></li>
      <li class="product" data-id="DC-1001"><h3>Marigold coaster set</h3><button class="add" type="button">Add to cart</button></li>
      <li class="product" data-id="DC-1013"><h3>Rani pink keychain</h3><button class="add" type="button">Add to cart</button></li>
    </ul>
    <section id="cart">
      <ul id="cart-lines"></ul>
      <p id="cart-empty">Your cart is empty.</p>
      <strong id="cart-total">₹0.00</strong>
    </section>
  </main>`;

/** Opens the page like a browser would: a fresh page, then createCart. localStorage is kept, as on a real reload. */
export function openPage() {
  document.body.innerHTML = PAGE;
  createCart(document.querySelector("#app"), CATALOGUE);
}

/** Clicks the Add button of one item. */
export function add(id) {
  document.querySelector(`.product[data-id="${id}"] .add`).click();
}

/** The lines on screen, as [{ id, qty }] with qty as the text shown. */
export function linesOnScreen() {
  return [...document.querySelectorAll("#cart-lines .line")].map((li) => ({
    id: li.dataset.id,
    qty: li.querySelector(".qty")?.textContent,
  }));
}

export function totalText() {
  return document.querySelector("#cart-total").textContent;
}

export function emptyMessageShown() {
  return !document.querySelector("#cart-empty").hidden;
}

/** What is saved under dhaaga:cart, as text, or "" when nothing is. */
export function savedText() {
  return localStorage.getItem("dhaaga:cart") ?? "";
}
