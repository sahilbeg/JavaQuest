// Shared helpers for the incident checks: open the sale grid, type into a card, refresh the stock.
import { createGrid } from "../src/grid.js";

export const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 4 };
export const coasters = { id: "DC-1001", name: "Marigold coaster set", paise: 15000, stock: 12 };
export const keychain = { id: "DC-1013", name: "Rani pink keychain", paise: 12000, stock: 7 };
export const booties = { id: "DC-1020", name: "Mint baby booties", paise: 35000, stock: 3 };

/** The same product with a new stock count, as a refresh would bring it. */
export const withStock = (product, stock) => ({ ...product, stock });

/** A fresh sale page with an empty grid. Returns the grid ({ update }). */
export function openGrid(onAdd = () => {}) {
  document.body.innerHTML = `<ul id="grid"></ul>`;
  return createGrid(document.querySelector("#grid"), onAdd);
}

export const cardFor = (id) => document.querySelector(`#grid .card[data-id="${id}"]`);
export const inputFor = (id) => cardFor(id)?.querySelector(".card__stitch-input");
export const ids = () => [...document.querySelectorAll("#grid .card")].map((card) => card.dataset.id);

/** Clicks into a card's name box and types text, like a customer. */
export function type(id, text) {
  const input = inputFor(id);
  input.focus();
  input.value = text;
  input.dispatchEvent(new Event("input", { bubbles: true }));
}

/** Presses Add to cart on a card. */
export function add(id) {
  cardFor(id).querySelector(".card__add").click();
}
