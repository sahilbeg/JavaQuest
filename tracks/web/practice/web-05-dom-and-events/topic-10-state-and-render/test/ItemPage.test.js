import { describe, it, expect, beforeEach } from "vitest";
import { itemTotal, createItemPage } from "../src/item-page.js";

const GOLU = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
const NO_EXTRAS = { qty: 1, giftWrap: false, stitch: "" };

beforeEach(() => {
  document.body.innerHTML = `
    <main id="item">
      <div class="stepper">
        <button type="button" data-action="dec" aria-label="One less">−</button>
        <span class="qty">1</span>
        <button type="button" data-action="inc" aria-label="One more">+</button>
      </div>
      <input type="checkbox" id="gift-wrap">
      <input id="stitch">
      <p id="summary"></p>
      <strong id="total"></strong>
    </main>`;
});

const $ = (selector) => document.querySelector(selector);
const open = (max = 5) => createItemPage($("#item"), GOLU, max);
const plus = (times = 1) => {
  for (let i = 0; i < times; i++) $('[data-action="inc"]').click();
};
const minus = () => $('[data-action="dec"]').click();
const typeStitch = (text) => {
  $("#stitch").value = text;
  $("#stitch").dispatchEvent(new Event("input", { bubbles: true }));
};

describe("itemTotal", () => {
  it("charges one Golu at 45000 paise", () => {
    expect(itemTotal(GOLU, NO_EXTRAS), "one Golu with no extras is 45000 paise").toBe(45000);
  });

  it("multiplies by the quantity", () => {
    expect(itemTotal(GOLU, { ...NO_EXTRAS, qty: 2 }), "two Golus are 90000 paise").toBe(90000);
  });

  it("adds gift wrap once, not once per piece", () => {
    expect(itemTotal(GOLU, { ...NO_EXTRAS, qty: 2, giftWrap: true }), "two Golus plus one gift wrap is 93000 paise").toBe(93000);
  });

  it("adds stitching to every piece", () => {
    expect(itemTotal(GOLU, { ...NO_EXTRAS, qty: 2, stitch: "Aarav" }), "two stitched Golus are (45000 + 5000) × 2 = 100000 paise").toBe(100000);
  });

  it("doesn't charge for a stitch name that is only spaces", () => {
    expect(itemTotal(GOLU, { ...NO_EXTRAS, stitch: "   " }), "a blank stitch name costs nothing").toBe(45000);
  });
});

describe("createItemPage", () => {
  it("draws the starting state", () => {
    open();
    expect($(".qty").textContent, "the quantity should start at 1").toBe("1");
    expect($("#summary").textContent, "the summary should say 1 × Golu the elephant").toBe("1 × Golu the elephant");
    expect($("#total").textContent, "the total should start at ₹450.00").toBe("₹450.00");
    expect($('[data-action="dec"]').disabled, "− should be off at 1").toBe(true);
  });

  it("follows the + button", () => {
    const page = open();
    plus(2);
    expect($(".qty").textContent, "two clicks on + should show 3").toBe("3");
    expect($("#summary").textContent, "the summary should say 3 × Golu the elephant").toBe("3 × Golu the elephant");
    expect($("#total").textContent, "three Golus are ₹1,350.00").toBe("₹1,350.00");
    expect(page.getState().qty, "the state should hold qty 3").toBe(3);
  });

  it("adds and removes gift wrap with the box", () => {
    const page = open();
    $("#gift-wrap").click();
    expect($("#total").textContent, "with gift wrap, one Golu is ₹480.00").toBe("₹480.00");
    expect(page.getState().giftWrap, "the state should say giftWrap is true").toBe(true);
    $("#gift-wrap").click();
    expect($("#total").textContent, "without gift wrap again, it is back to ₹450.00").toBe("₹450.00");
  });

  it("counts gift wrap once, whatever the order of the clicks", () => {
    open();
    $("#gift-wrap").click();
    plus();
    expect($("#total").textContent, "wrap first, then +: two Golus and one wrap are ₹930.00").toBe("₹930.00");
  });

  it("counts gift wrap once when + comes first", () => {
    open();
    plus();
    $("#gift-wrap").click();
    expect($("#total").textContent, "+ first, then wrap: still ₹930.00").toBe("₹930.00");
  });

  it("adds stitching as the customer types", () => {
    const page = open();
    typeStitch("Aarav");
    expect($("#total").textContent, "one stitched Golu is ₹500.00").toBe("₹500.00");
    expect(page.getState().stitch, "the state should hold the typed name").toBe("Aarav");
  });

  it("puts every choice together", () => {
    open();
    plus();
    $("#gift-wrap").click();
    typeStitch("Aarav");
    expect($("#total").textContent, "two stitched Golus plus gift wrap are ₹1,030.00").toBe("₹1,030.00");
  });

  it("stops at the maximum", () => {
    const page = open(5);
    plus(10);
    expect(page.getState().qty, "the quantity should stop at 5").toBe(5);
    expect($('[data-action="inc"]').disabled, "+ should be off at the maximum").toBe(true);
  });

  it("never goes below 1", () => {
    const page = open();
    minus();
    expect(page.getState().qty, "the quantity should stay at 1").toBe(1);
    expect($(".qty").textContent, "the page should still show 1").toBe("1");
  });

  it("makes a new state object for each change", () => {
    const page = open();
    const first = page.getState();
    plus();
    expect(page.getState() !== first, "update should build a new state object with { ...state, ...changes }").toBe(true);
    expect(first.qty, "the old state object should not change").toBe(1);
  });
});
