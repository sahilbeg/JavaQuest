import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { createStore } from "../src/store.js";
import { subtotal, deliveryFee, orderTotal, detailsErrors, furthestStep, canVisit, createCheckout } from "../src/checkout.js";

const golu = { id: "DC-1042", qty: 1 };
const twoCoasters = { id: "DC-2001", qty: 2 };
const goodDetails = { name: "Asha Kulkarni", phone: "98765 43210", pin: "411004" };
const noDetails = { name: "", phone: "", pin: "" };

const stateWith = (changes = {}) => ({ lines: [golu, twoCoasters], details: noDetails, paid: false, ...changes });

describe("money", () => {
  it("adds up the cart in paise", () => {
    expect(subtotal([golu, twoCoasters]), "one Golu (45000) and two coaster sets (2 × 15000) make 75000 paise").toBe(75000);
    expect(subtotal([]), "an empty cart is 0").toBe(0);
  });

  it("charges ₹60 delivery up to ₹999, and nothing above or for an empty cart", () => {
    expect(deliveryFee(75000), "₹750 pays ₹60 delivery, 6000 paise").toBe(6000);
    expect(deliveryFee(99900), "exactly ₹999 still pays delivery").toBe(6000);
    expect(deliveryFee(99901), "above ₹999 delivery is free").toBe(0);
    expect(deliveryFee(0), "an empty cart pays no delivery").toBe(0);
  });

  it("gives the total the customer pays", () => {
    expect(orderTotal([golu, twoCoasters]), "75000 plus 6000 delivery is 81000 paise, ₹810.00").toBe(81000);
    expect(orderTotal([{ id: "DC-3001", qty: 2 }]), "two tote bags are 179800 paise, so delivery is free").toBe(179800);
  });
});

describe("detailsErrors", () => {
  it("finds no problems in good details", () => {
    expect(detailsErrors(goodDetails), "a name, a 10-digit phone and a 6-digit PIN have no errors").toEqual({});
  });

  it("names every empty field", () => {
    expect(detailsErrors(noDetails), "empty details should give all three messages").toEqual({
      name: "Please tell us your name.",
      phone: "A phone number has 10 digits, like 98765 43210.",
      pin: "A PIN code has 6 digits, like 411004.",
    });
  });

  it("checks the shape of the phone and the PIN", () => {
    const details = { name: "Asha", phone: "98765", pin: "011004" };
    expect(Object.keys(detailsErrors(details)).sort(), "a short phone and a PIN starting with 0 are both wrong").toEqual(["phone", "pin"]);
  });
});

describe("which steps are open", () => {
  it("knows the furthest step the customer may reach", () => {
    expect(furthestStep(stateWith({ lines: [] })), "with an empty cart, only the cart step is open").toBe("cart");
    expect(furthestStep(stateWith()), "with items but no details, the customer can reach details").toBe("details");
    expect(furthestStep(stateWith({ details: goodDetails })), "with good details, the customer can reach pay").toBe("pay");
    expect(furthestStep(stateWith({ details: goodDetails, paid: true })), "once paid, the order is done").toBe("done");
  });

  it("opens every step up to the furthest one", () => {
    const state = stateWith({ details: goodDetails });
    expect(canVisit(state, "cart"), "the cart is always open before paying").toBe(true);
    expect(canVisit(state, "pay"), "pay is open with good details").toBe(true);
    expect(canVisit(stateWith(), "pay"), "pay is closed while the details have errors").toBe(false);
    expect(canVisit(state, "done"), "done is closed until the customer pays").toBe(false);
  });

  it("closes everything except done after paying, and unknown steps always", () => {
    const paid = stateWith({ details: goodDetails, paid: true });
    expect(canVisit(paid, "pay"), "after paying, pay must be closed, so no one pays twice").toBe(false);
    expect(canVisit(paid, "done"), "after paying, done is open").toBe(true);
    expect(canVisit(stateWith(), "shipping"), "\"shipping\" isn't a step").toBe(false);
  });
});

describe("the checkout router", () => {
  let checkout;
  let outlet;

  beforeEach(() => {
    document.body.innerHTML = `<main id="app"></main>`;
    document.title = "";
    outlet = document.querySelector("#app");
  });

  afterEach(() => {
    checkout?.stop();
    checkout = undefined;
    vi.restoreAllMocks();
  });

  const shownStep = () => outlet.querySelector("[data-step]")?.dataset.step;

  function openAt(path, state) {
    history.replaceState(null, "", path);
    const store = createStore(state);
    checkout = createCheckout(outlet, store);
    return store;
  }

  it("opens the step in the address, with its title", () => {
    openAt("/checkout/cart", stateWith());
    checkout.start();
    expect(shownStep(), "/checkout/cart should show the cart step").toBe("cart");
    expect(document.title, "the tab should say \"Checkout: Your cart | Dhaaga & Co.\"").toBe("Checkout: Your cart | Dhaaga & Co.");
  });

  it("sends a closed step to the furthest open one, replacing the address", () => {
    openAt("/checkout/pay", stateWith());
    const push = vi.spyOn(history, "pushState");
    checkout.start();
    expect(shownStep(), "pay is closed without details, so the details step should show").toBe("details");
    expect(location.pathname, "the address should be swapped to /checkout/details").toBe("/checkout/details");
    expect(push.mock.calls.length, "the swap must use replaceState, so Back can't return to the closed step").toBe(0);
  });

  it("opens the furthest step for a plain /checkout address", () => {
    openAt("/checkout", stateWith({ details: goodDetails }));
    checkout.start();
    expect(location.pathname, "/checkout alone should become /checkout/pay").toBe("/checkout/pay");
    expect(shownStep(), "the pay step should show").toBe("pay");
  });

  it("moves forward with go, adding a history entry", () => {
    openAt("/checkout/cart", stateWith());
    checkout.start();
    const push = vi.spyOn(history, "pushState");
    checkout.go("details");
    expect(push.mock.calls[0]?.[2], "go(\"details\") should push /checkout/details").toBe("/checkout/details");
    expect(shownStep(), "the details step should show").toBe("details");
    expect(document.title, "the title should follow").toBe("Checkout: Your details | Dhaaga & Co.");
  });

  it("adds no entry for the step already showing", () => {
    openAt("/checkout/cart", stateWith());
    checkout.start();
    const push = vi.spyOn(history, "pushState");
    checkout.go("cart");
    expect(push.mock.calls.length, "go to the step you are on should push nothing").toBe(0);
  });

  it("follows Back", () => {
    openAt("/checkout/cart", stateWith());
    checkout.start();
    checkout.go("details");
    history.replaceState(null, "", "/checkout/cart");
    window.dispatchEvent(new PopStateEvent("popstate"));
    expect(shownStep(), "Back to /checkout/cart should show the cart step").toBe("cart");
  });

  it("never shows pay again after paying, even on Back", () => {
    const store = openAt("/checkout/pay", stateWith({ details: goodDetails }));
    checkout.start();
    store.setState((s) => ({ ...s, paid: true }));
    checkout.go("done");
    history.replaceState(null, "", "/checkout/pay");
    window.dispatchEvent(new PopStateEvent("popstate"));
    expect(shownStep(), "Back to pay after paying should land on done").toBe("done");
    expect(location.pathname, "the address should be swapped to /checkout/done").toBe("/checkout/done");
  });

  it("stops listening after stop()", () => {
    openAt("/checkout/cart", stateWith());
    checkout.start();
    checkout.stop();
    history.replaceState(null, "", "/checkout/details");
    window.dispatchEvent(new PopStateEvent("popstate"));
    expect(shownStep(), "after stop(), Back must not redraw").toBe("cart");
  });
});
