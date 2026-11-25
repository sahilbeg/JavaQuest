import { describe, it, expect, beforeEach } from "vitest";
import { startStore } from "../src/app.js";
import { PRODUCTS } from "../src/catalogue.js";

let root;

beforeEach(() => {
  document.body.innerHTML = `<div id="app"></div>`;
  root = document.querySelector("#app");
  startStore(root, PRODUCTS);
});

/** The field whose <label> says exactly this, like a screen reader finds it. */
function field(labelText) {
  const label = [...root.querySelectorAll("label")].find((element) => element.textContent === labelText);
  return label ? document.getElementById(label.htmlFor) : null;
}

/** The button with this name: its aria-label, or its text when it has none. */
function button(name) {
  return [...root.querySelectorAll("button")].find((element) => (element.getAttribute("aria-label") ?? element.textContent) === name) ?? null;
}

function type(labelText, text) {
  const input = field(labelText);
  if (input) {
    input.value = text;
    input.dispatchEvent(new Event("input", { bubbles: true }));
  }
}

function choose(labelText, value) {
  const select = field(labelText);
  if (select) {
    select.value = value;
    select.dispatchEvent(new Event("change", { bubbles: true }));
  }
}

const cartCount = () => root.querySelector("#cart-count")?.textContent;
const totals = () => [...root.querySelectorAll("#cart-totals p")].map((p) => p.textContent);

describe("Stage 5: the header and footer", () => {
  it("starts the page with a skip link to the main content", () => {
    const firstLink = root.querySelector("a");
    expect(firstLink?.textContent, "the first link on the page should be Skip to content").toBe("Skip to content");
    expect(firstLink?.getAttribute("href"), "the skip link should go to #main").toBe("#main");
    expect(root.querySelector("main#main"), "there should be a <main id=\"main\"> for it to skip to").not.toBeNull();
  });

  it("has a labelled main menu and a footer", () => {
    expect(root.querySelector('nav[aria-label="Main"]'), "the menu should be a <nav aria-label=\"Main\">").not.toBeNull();
    expect(root.querySelector("footer"), "the page should end with the footer").not.toBeNull();
  });

  it("counts every item in the header as they're added", () => {
    expect(cartCount(), "an empty cart should say Cart: 0 items").toBe("Cart: 0 items");
    button("Add Golu the elephant to cart")?.click();
    expect(cartCount(), "one Golu should say Cart: 1 item").toBe("Cart: 1 item");
    button("Add Golu the elephant to cart")?.click();
    button("Add Rani pink keychain to cart")?.click();
    expect(cartCount(), "two Golus and a keychain should say Cart: 3 items").toBe("Cart: 3 items");
  });
});

describe("Stage 5: a whole customer journey", () => {
  it("finds Golu, adds coasters, checks out, pays by UPI and comes back to an empty cart", () => {
    type("Search", "golu");
    expect(root.querySelectorAll(".product").length, "searching golu should show one product").toBe(1);
    button("Add Golu the elephant to cart")?.click();

    type("Search", "");
    choose("Category", "Home");
    button("Add Indigo coaster set to cart")?.click();

    choose("Quantity for Golu the elephant", "2");
    expect(cartCount(), "two Golus and a coaster set make Cart: 3 items").toBe("Cart: 3 items");
    expect(totals(), "₹1,050.00 is above ₹999, so delivery is free").toEqual(["Subtotal: ₹1,050.00", "Delivery: Free", "Total: ₹1,050.00"]);

    button("Checkout")?.click();
    expect(root.querySelector("#search"), "checkout should replace the shop").toBeNull();
    expect(document.activeElement?.textContent, "focus should be on the checkout's first heading").toBe("Delivery address");

    type("Name", "Meera Kulkarni");
    type("Phone", "98765 43210");
    type("PIN code", "411007");
    button("Continue")?.click();
    expect(button("Pay ₹1,050.00"), "the pay button should show the cart's total").not.toBeNull();

    type("UPI ID", "meera@okaxis");
    button("Pay ₹1,050.00")?.click();
    expect(document.activeElement?.textContent, "focus should be on Thank you!").toBe("Thank you!");
    expect(root.textContent.includes("order-17"), "the first order should be order-17").toBe(true);
    expect(cartCount(), "a placed order should empty the cart").toBe("Cart: 0 items");

    button("Back to the shop")?.click();
    expect(root.querySelector("#search"), "Back to the shop should show the shop again").not.toBeNull();
    expect(root.querySelector("#cart-empty")?.hidden, "the cart should be empty after paying").toBe(false);
  });

  it("keeps the cart when the customer goes back from checkout", () => {
    button("Add Rani pink keychain to cart")?.click();
    button("Checkout")?.click();
    button("Back to the shop")?.click();
    expect(cartCount(), "going back should keep the keychain: Cart: 1 item").toBe("Cart: 1 item");
    expect([...root.querySelectorAll("#cart-lines .line")].map((li) => li.dataset.id), "the cart should still hold the keychain").toEqual(["DC-1210"]);
  });
});
