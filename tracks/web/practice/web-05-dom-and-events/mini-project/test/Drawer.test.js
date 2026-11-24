import { describe, it, expect, beforeEach } from "vitest";
import { createDrawer } from "../src/drawer.js";

const CATALOGUE = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1001", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-1013", name: "Rani pink keychain", paise: 12000 },
];

beforeEach(() => {
  document.body.innerHTML = `
    <main id="app">
      <button id="cart-toggle" type="button" aria-controls="cart-drawer" aria-expanded="false">Cart (0)</button>
      <ul id="products">
        <li class="product" data-id="DC-1042"><h3>Golu the elephant</h3><button class="add" type="button">Add to cart</button></li>
        <li class="product" data-id="DC-1001"><h3>Marigold coaster set</h3><button class="add" type="button">Add to cart</button></li>
        <li class="product" data-id="DC-1013"><h3>Rani pink keychain</h3><button class="add" type="button">Add to cart</button></li>
      </ul>
      <aside id="cart-drawer" hidden>
        <h2>Your cart</h2>
        <button id="cart-close" type="button">Close</button>
        <ul id="drawer-lines"></ul>
        <p id="drawer-empty">Your cart is empty.</p>
        <strong id="drawer-total">₹0.00</strong>
      </aside>
    </main>`;
});

const $ = (selector) => document.querySelector(selector);
const open = (startLines = []) => createDrawer($("#app"), CATALOGUE, startLines);
const add = (id) => $(`.product[data-id="${id}"] .add`).click();
const lineButton = (id, action) => $(`#drawer-lines .line[data-id="${id}"] [data-action="${action}"]`);
const lineText = (id, selector) => $(`#drawer-lines .line[data-id="${id}"] ${selector}`)?.textContent;
const lineIds = () => [...document.querySelectorAll("#drawer-lines .line")].map((li) => li.dataset.id);

describe("the cart button", () => {
  it("starts closed", () => {
    open();
    expect($("#cart-drawer").hidden, "the drawer should start hidden").toBe(true);
    expect($("#cart-toggle").getAttribute("aria-expanded"), "aria-expanded should start as \"false\"").toBe("false");
    expect($("#cart-toggle").textContent, "an empty cart's button should say Cart (0)").toBe("Cart (0)");
  });

  it("opens the drawer and moves focus to Close", () => {
    open();
    $("#cart-toggle").click();
    expect($("#cart-drawer").hidden, "after a click the drawer should show").toBe(false);
    expect($("#cart-toggle").getAttribute("aria-expanded"), "aria-expanded should become \"true\"").toBe("true");
    expect(document.activeElement === $("#cart-close"), "focus should move to the Close button when the drawer opens").toBe(true);
  });

  it("closes the drawer on a second click", () => {
    open();
    $("#cart-toggle").click();
    $("#cart-toggle").click();
    expect($("#cart-drawer").hidden, "a second click should hide the drawer").toBe(true);
    expect($("#cart-toggle").getAttribute("aria-expanded"), "aria-expanded should go back to \"false\"").toBe("false");
  });
});

describe("closing the drawer", () => {
  it("closes with the Close button and returns focus to the cart button", () => {
    open();
    $("#cart-toggle").click();
    $("#cart-close").click();
    expect($("#cart-drawer").hidden, "Close should hide the drawer").toBe(true);
    expect($("#cart-toggle").getAttribute("aria-expanded"), "aria-expanded should be \"false\" after Close").toBe("false");
    expect(document.activeElement === $("#cart-toggle"), "focus should go back to the cart button").toBe(true);
  });

  it("closes when Escape is pressed inside the drawer", () => {
    open();
    $("#cart-toggle").click();
    $("#cart-close").dispatchEvent(new KeyboardEvent("keydown", { key: "Escape", bubbles: true }));
    expect($("#cart-drawer").hidden, "Escape should hide the drawer").toBe(true);
    expect(document.activeElement === $("#cart-toggle"), "after Escape, focus should go back to the cart button").toBe(true);
  });
});

describe("adding from the catalogue", () => {
  it("counts the pieces on the cart button", () => {
    open();
    add("DC-1042");
    add("DC-1042");
    add("DC-1013");
    expect($("#cart-toggle").textContent, "2 Golus and a keychain make Cart (3)").toBe("Cart (3)");
  });

  it("doesn't open the drawer", () => {
    open();
    add("DC-1042");
    expect($("#cart-drawer").hidden, "Add should not open the drawer").toBe(true);
  });

  it("lists the lines with their quantities and amounts", () => {
    open();
    add("DC-1042");
    add("DC-1001");
    add("DC-1042");
    expect(lineIds(), "Golu was added first, then the coaster set").toEqual(["DC-1042", "DC-1001"]);
    expect(lineText("DC-1042", ".name"), "the line should show the item's name").toBe("Golu the elephant");
    expect(lineText("DC-1042", ".qty"), "Golu was added twice, so his quantity is 2").toBe("2");
    expect(lineText("DC-1042", ".amount"), "two Golus cost ₹900.00").toBe("₹900.00");
    expect($("#drawer-total").textContent, "two Golus and a coaster set total ₹1,050.00").toBe("₹1,050.00");
  });

  it("labels the + and − buttons for screen readers", () => {
    open();
    add("DC-1042");
    expect(lineButton("DC-1042", "inc")?.getAttribute("aria-label"), "+ should be labelled One more Golu the elephant").toBe("One more Golu the elephant");
    expect(lineButton("DC-1042", "dec")?.getAttribute("aria-label"), "− should be labelled One less Golu the elephant").toBe("One less Golu the elephant");
  });
});

describe("the + and − buttons", () => {
  it("adds one with +", () => {
    open([{ id: "DC-1042", qty: 2 }]);
    lineButton("DC-1042", "inc")?.click();
    expect(lineText("DC-1042", ".qty"), "+ should make 3 Golus").toBe("3");
    expect($("#drawer-total").textContent, "three Golus total ₹1,350.00").toBe("₹1,350.00");
    expect($("#cart-toggle").textContent, "the cart button should say Cart (3)").toBe("Cart (3)");
  });

  it("takes one away with −", () => {
    open([{ id: "DC-1042", qty: 2 }]);
    lineButton("DC-1042", "dec")?.click();
    expect(lineText("DC-1042", ".qty"), "− should leave 1 Golu").toBe("1");
  });

  it("removes the line when the last piece is taken away", () => {
    const drawer = open([{ id: "DC-1042", qty: 2 }, { id: "DC-1001", qty: 1 }]);
    lineButton("DC-1001", "dec")?.click();
    expect(lineIds(), "the coaster set line should be gone").toEqual(["DC-1042"]);
    expect(drawer.getLines(), "the state should hold only Golu × 2").toEqual([{ id: "DC-1042", qty: 2 }]);
  });
});

describe("the empty cart", () => {
  it("shows the empty message when there is nothing in the cart", () => {
    open();
    expect($("#drawer-empty").hidden, "an empty cart should show Your cart is empty.").toBe(false);
    expect($("#drawer-total").textContent, "an empty cart totals ₹0.00").toBe("₹0.00");
  });

  it("hides the message once something is added", () => {
    open();
    add("DC-1013");
    expect($("#drawer-empty").hidden, "with a keychain in the cart, the empty message should hide").toBe(true);
  });

  it("shows the message again when the last line is removed", () => {
    open([{ id: "DC-1013", qty: 1 }]);
    lineButton("DC-1013", "dec")?.click();
    expect(lineIds(), "no lines should be left").toEqual([]);
    expect($("#drawer-empty").hidden, "the empty message should come back").toBe(false);
    expect($("#drawer-total").textContent, "the total should be back to ₹0.00").toBe("₹0.00");
  });
});

describe("starting lines", () => {
  it("draws the lines it starts with", () => {
    open([{ id: "DC-1001", qty: 2 }]);
    expect($("#cart-toggle").textContent, "two coaster sets make Cart (2)").toBe("Cart (2)");
    expect($("#drawer-total").textContent, "two coaster sets total ₹300.00").toBe("₹300.00");
  });
});
