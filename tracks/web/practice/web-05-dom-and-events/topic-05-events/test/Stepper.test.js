import { describe, it, expect, beforeEach } from "vitest";
import { setupStepper, setupAddButton, setupEscapeClears } from "../src/stepper.js";

beforeEach(() => {
  document.body.innerHTML = `
    <div class="stepper" id="first">
      <button class="minus" type="button" aria-label="One less">−</button>
      <span class="qty">1</span>
      <button class="plus" type="button" aria-label="One more">+</button>
    </div>
    <div class="stepper" id="second">
      <button class="minus" type="button" aria-label="One less">−</button>
      <span class="qty">1</span>
      <button class="plus" type="button" aria-label="One more">+</button>
    </div>
    <input id="note" type="text">
    <button id="add" type="button">Add to cart</button>
    <p id="status" role="status"></p>`;
});

const part = (id, selector) => document.querySelector(`#${id} ${selector}`);
const qtyText = (id = "first") => part(id, ".qty").textContent;
const clickPlus = (times, id = "first") => {
  for (let i = 0; i < times; i++) part(id, ".plus").click();
};

describe("setupStepper", () => {
  it("starts at 1 with the minus button switched off", () => {
    setupStepper(document.querySelector("#first"), 5);
    expect(qtyText(), "the quantity should start at 1").toBe("1");
    expect(part("first", ".minus").disabled, "you can't go below 1, so minus should be disabled at the start").toBe(true);
    expect(part("first", ".plus").disabled, "plus should work at the start").toBe(false);
  });

  it("adds one for each click on plus", () => {
    const readQty = setupStepper(document.querySelector("#first"), 5);
    clickPlus(2);
    expect(qtyText(), "two clicks on + should show 3").toBe("3");
    expect(readQty(), "the returned function should read 3").toBe(3);
  });

  it("takes one away for a click on minus", () => {
    const readQty = setupStepper(document.querySelector("#first"), 5);
    clickPlus(2);
    part("first", ".minus").click();
    expect(qtyText(), "+ + − should show 2").toBe("2");
    expect(readQty(), "the returned function should read 2").toBe(2);
  });

  it("switches minus back on once the quantity is above 1", () => {
    setupStepper(document.querySelector("#first"), 5);
    clickPlus(1);
    expect(part("first", ".minus").disabled, "at 2 you can go down again, so minus should work").toBe(false);
  });

  it("never goes below 1", () => {
    const readQty = setupStepper(document.querySelector("#first"), 5);
    clickPlus(1);
    part("first", ".minus").click();
    part("first", ".minus").click();
    expect(qtyText(), "the quantity should stop at 1").toBe("1");
    expect(readQty(), "the quantity should stop at 1").toBe(1);
  });

  it("stops at the maximum and switches plus off", () => {
    const readQty = setupStepper(document.querySelector("#first"), 3);
    clickPlus(5);
    expect(qtyText(), "with a maximum of 3, the quantity should stop at 3").toBe("3");
    expect(readQty(), "with a maximum of 3, the quantity should stop at 3").toBe(3);
    expect(part("first", ".plus").disabled, "plus should be disabled at the maximum").toBe(true);
  });

  it("gives each stepper its own quantity", () => {
    setupStepper(document.querySelector("#first"), 5);
    setupStepper(document.querySelector("#second"), 5);
    clickPlus(1, "first");
    expect(qtyText("first"), "the first stepper was clicked once, so it shows 2").toBe("2");
    expect(qtyText("second"), "the second stepper was never clicked, so it still shows 1").toBe("1");
  });
});

describe("setupAddButton", () => {
  it("shows nothing until the button is clicked", () => {
    setupAddButton(document.querySelector("#add"), document.querySelector("#status"), () => 3);
    expect(document.querySelector("#status").textContent, "the status should be empty before any click").toBe("");
  });

  it("says how many were added", () => {
    setupAddButton(document.querySelector("#add"), document.querySelector("#status"), () => 3);
    document.querySelector("#add").click();
    expect(document.querySelector("#status").textContent, "after a click the status should say Added 3 to your cart").toBe("Added 3 to your cart");
  });

  it("reads the quantity at the moment of the click", () => {
    let qty = 1;
    setupAddButton(document.querySelector("#add"), document.querySelector("#status"), () => qty);
    qty = 4;
    document.querySelector("#add").click();
    expect(document.querySelector("#status").textContent, "the customer picked 4 before clicking, so it should say Added 4: call readQty() inside the handler").toBe("Added 4 to your cart");
  });
});

describe("setupEscapeClears", () => {
  const press = (key) => document.querySelector("#note").dispatchEvent(new KeyboardEvent("keydown", { key, bubbles: true }));

  it("empties the gift note when Escape is pressed", () => {
    setupEscapeClears(document.querySelector("#note"));
    document.querySelector("#note").value = "Happy Diwali, Riya!";
    press("Escape");
    expect(document.querySelector("#note").value, "Escape should empty the box").toBe("");
  });

  it("leaves the note alone for other keys", () => {
    setupEscapeClears(document.querySelector("#note"));
    document.querySelector("#note").value = "Happy Diwali, Riya!";
    press("Enter");
    expect(document.querySelector("#note").value, "Enter should not change the note").toBe("Happy Diwali, Riya!");
  });
});
