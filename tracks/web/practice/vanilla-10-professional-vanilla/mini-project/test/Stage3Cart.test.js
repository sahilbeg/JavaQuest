import { describe, it, expect, beforeEach, vi } from "vitest";
import { cartReducer } from "../src/cart.js";
import { createStore } from "../src/store.js";
import { mountCart } from "../src/cartView.js";
import { PRODUCTS } from "../src/catalogue.js";

const golu = PRODUCTS[0];
const booties = PRODUCTS[5];
const goluLine = (qty) => ({ id: "DC-1042", name: "Golu the elephant", paise: 45000, qty });
const bootiesLine = (qty) => ({ id: "DC-1410", name: "Off-white baby booties", paise: 35000, qty });

describe("Stage 3: cartReducer", () => {
  it("adds a new product as a line with qty 1 and only the fields the cart needs", () => {
    expect(cartReducer([], { type: "add", product: golu }), "adding Golu should give one line: id, name, paise and qty 1, with no category").toEqual([goluLine(1)]);
  });

  it("adds new products at the end", () => {
    expect(cartReducer([goluLine(1)], { type: "add", product: booties }), "the booties should come after Golu").toEqual([goluLine(1), bootiesLine(1)]);
  });

  it("adds one more of a product already in the cart, up to 5", () => {
    expect(cartReducer([goluLine(2)], { type: "add", product: golu }), "adding Golu again should make 3").toEqual([goluLine(3)]);
    expect(cartReducer([goluLine(5)], { type: "add", product: golu }), "5 is the most anyone can order, so Golu stays at 5").toEqual([goluLine(5)]);
  });

  it("sets, removes and clears lines", () => {
    const lines = [goluLine(1), bootiesLine(2)];
    expect(cartReducer(lines, { type: "setQty", id: "DC-1042", qty: 4 }), "setQty should change only Golu's qty").toEqual([goluLine(4), bootiesLine(2)]);
    expect(cartReducer(lines, { type: "remove", id: "DC-1042" }), "remove should drop Golu's line").toEqual([bootiesLine(2)]);
    expect(cartReducer(lines, { type: "clear" }), "clear should empty the cart").toEqual([]);
  });

  it("never changes the lines it is given", () => {
    const lines = [goluLine(1), bootiesLine(2)];
    const before = JSON.parse(JSON.stringify(lines));
    cartReducer(lines, { type: "add", product: golu });
    cartReducer(lines, { type: "setQty", id: "DC-1410", qty: 5 });
    cartReducer(lines, { type: "remove", id: "DC-1042" });
    expect(lines, "the old lines must stay as they were; return new arrays and objects").toEqual(before);
  });
});

describe("Stage 3: the cart", () => {
  let section;
  let onCheckout;
  let store;

  beforeEach(() => {
    document.body.innerHTML = `<section id="cart"></section>`;
    section = document.querySelector("#cart");
    onCheckout = vi.fn();
  });

  function mount(lines) {
    store = createStore(cartReducer, lines);
    mountCart(section, store, onCheckout);
  }

  const totals = () => [...section.querySelectorAll("#cart-totals p")].map((p) => p.textContent);
  const select = (id) => section.querySelector(`#qty-${id}`);

  function choose(id, qty) {
    select(id).value = String(qty);
    select(id).dispatchEvent(new Event("change", { bubbles: true }));
  }

  it("shows an empty cart with a disabled Checkout button", () => {
    mount([]);
    expect(section.querySelector("#cart-empty")?.hidden, "an empty cart should show Your cart is empty.").toBe(false);
    expect(section.querySelector("#checkout")?.disabled, "you can't check out an empty cart").toBe(true);
  });

  it("gives each line a quantity select with its own label", () => {
    mount([goluLine(2)]);
    expect(select("DC-1042")?.value, "Golu's select should show 2").toBe("2");
    expect(section.querySelector('label[for="qty-DC-1042"]')?.textContent, "the select needs <label for=\"qty-DC-1042\">Quantity for Golu the elephant</label>").toBe("Quantity for Golu the elephant");
    expect(section.querySelector('.line[data-id="DC-1042"] .amount')?.textContent, "two Golus cost ₹900.00").toBe("₹900.00");
    expect(section.querySelector("#cart-empty")?.hidden, "the empty message should hide when there are lines").toBe(true);
  });

  it("names each Remove button after its item", () => {
    mount([goluLine(1)]);
    expect(section.querySelector("button.remove")?.getAttribute("aria-label"), "the button should be named Remove Golu the elephant").toBe("Remove Golu the elephant");
  });

  it("shows the totals with delivery for a small order", () => {
    mount([goluLine(1)]);
    expect(totals(), "one Golu: subtotal, ₹60 delivery and the total").toEqual(["Subtotal: ₹450.00", "Delivery: ₹60.00", "Total: ₹510.00"]);
    expect(section.querySelector("#checkout")?.disabled, "Checkout should work with Golu in the cart").toBe(false);
  });

  it("shows the Diwali offer and free delivery on a big order", () => {
    mount([goluLine(4)]);
    expect(totals(), "four Golus: ₹180.00 off and free delivery").toEqual(["Subtotal: ₹1,800.00", "Diwali offer: -₹180.00", "Delivery: Free", "Total: ₹1,620.00"]);
  });

  it("changes a quantity through the store", () => {
    mount([goluLine(1)]);
    choose("DC-1042", 3);
    expect(store.getState(), "the store should hold Golu with qty 3, a number, not the text \"3\"").toEqual([goluLine(3)]);
    expect(section.querySelector('.line[data-id="DC-1042"] .amount')?.textContent, "three Golus cost ₹1,350.00").toBe("₹1,350.00");
  });

  it("keeps focus on the quantity select after it changes", () => {
    mount([goluLine(1)]);
    select("DC-1042").focus();
    choose("DC-1042", 2);
    expect(document.activeElement?.id, "after the cart redraws, focus should be back on Golu's quantity select").toBe("qty-DC-1042");
  });

  it("removes a line and moves focus to the cart heading", () => {
    mount([goluLine(1), bootiesLine(1)]);
    section.querySelector('.line[data-id="DC-1042"] button.remove')?.click();
    expect(store.getState(), "only the booties should be left in the store").toEqual([bootiesLine(1)]);
    expect([...section.querySelectorAll(".line")].map((li) => li.dataset.id), "only the booties' line should be drawn").toEqual(["DC-1410"]);
    expect(document.activeElement?.id, "focus should move to the cart heading, not fall to the top of the page").toBe("cart-heading");
  });

  it("opens checkout from the Checkout button", () => {
    mount([goluLine(1)]);
    section.querySelector("#checkout")?.click();
    expect(onCheckout.mock.calls.length, "Checkout should call onCheckout once").toBe(1);
  });
});
