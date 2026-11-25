import { describe, it, expect, beforeEach } from "vitest";
import { createStore } from "../src/store.js";
import { addLine, createCatalogue } from "../src/catalogue.js";

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 4 };
const coasters = { id: "DC-1001", name: "Marigold coaster set", paise: 15000, stock: 12 };
const keychain = { id: "DC-1013", name: "Rani pink keychain", paise: 12000, stock: 7 };
const booties = { id: "DC-1020", name: "Mint baby booties", paise: 35000, stock: 3 };

let root;

beforeEach(() => {
  document.body.innerHTML = `<div id="catalogue"></div>`;
  root = document.querySelector("#catalogue");
});

function open(products) {
  const store = createStore({ products, lines: [] });
  createCatalogue(root, store);
  return store;
}

const cards = () => [...root.querySelectorAll("product-card")];
const cardFor = (id) => root.querySelector(`product-card[data-id="${id}"]`);

/** What a card sends when its Add button is pressed. */
function announceAdd(id) {
  cardFor(id)?.dispatchEvent(new CustomEvent("add-to-cart", { detail: { id }, bubbles: true, composed: true }));
}

describe("addLine", () => {
  it("adds a new line with qty 1", () => {
    expect(addLine([], "DC-1042"), "adding Golu to an empty cart should give one line with qty 1").toEqual([{ id: "DC-1042", qty: 1 }]);
  });

  it("adds one to an existing line, in a new array", () => {
    const lines = [{ id: "DC-1042", qty: 1 }, { id: "DC-1001", qty: 2 }];
    const next = addLine(lines, "DC-1042");
    expect(next, "Golu's line should go up to 2, and the coasters stay").toEqual([{ id: "DC-1042", qty: 2 }, { id: "DC-1001", qty: 2 }]);
    expect(next === lines, "addLine should return a new array").toBe(false);
    expect(lines[0].qty, "the old line must not change").toBe(1);
  });
});

describe("createCatalogue", () => {
  it("draws one product-card per product, in order, with its product", () => {
    open([golu, coasters, keychain]);
    expect(cards().map((card) => card.dataset.id), "the cards' data-ids should follow the products").toEqual(["DC-1042", "DC-1001", "DC-1013"]);
    expect(cardFor("DC-1001")?.product, "each card's product property should be its product").toBe(coasters);
  });

  it("adds to the cart when a card sends add-to-cart", () => {
    const store = open([golu, coasters]);
    announceAdd("DC-1042");
    announceAdd("DC-1042");
    expect(store.getState().lines, "two adds of Golu should give one line with qty 2").toEqual([{ id: "DC-1042", qty: 2 }]);
  });

  it("keeps the same card when the stock changes", () => {
    const store = open([golu, coasters]);
    const card = cardFor("DC-1042");
    store.setState({ products: [{ ...golu, stock: 2 }, coasters] });
    expect(card, "the catalogue should draw a card for Golu").not.toBeNull();
    expect(cardFor("DC-1042"), "Golu should still be shown by the very same <product-card>").toBe(card);
    expect(card?.product?.stock, "the kept card should get the new product, with stock 2").toBe(2);
  });

  it("adds, removes and reorders cards to follow the products", () => {
    const store = open([golu, coasters, keychain]);
    const goluCard = cardFor("DC-1042");
    store.setState({ products: [keychain, booties, golu] });
    expect(cards().map((card) => card.dataset.id), "the cards should be the keychain, the booties and Golu").toEqual(["DC-1013", "DC-1020", "DC-1042"]);
    expect(cardFor("DC-1042"), "Golu's card should be kept while moving").toBe(goluCard);
  });

  it("listens once, however often the products change", () => {
    const store = open([golu]);
    store.setState({ products: [{ ...golu, stock: 3 }] });
    store.setState({ products: [{ ...golu, stock: 2 }] });
    store.setState({ products: [{ ...golu, stock: 1 }] });
    announceAdd("DC-1042");
    expect(store.getState().lines, "one add-to-cart should add exactly one Golu").toEqual([{ id: "DC-1042", qty: 1 }]);
  });
});
