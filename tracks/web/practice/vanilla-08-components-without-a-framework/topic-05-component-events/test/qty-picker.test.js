import { describe, it, expect, afterEach, vi } from "vitest";
import { listenForAddToCart } from "../src/qty-picker.js";

const cleanups = [];

afterEach(() => {
  while (cleanups.length > 0) {
    cleanups.pop()();
  }
  document.body.innerHTML = "";
});

/** A picker for one item, with any extra attributes, inside parent (the page by default). */
function picker(attributes = {}, parent = document.body) {
  const element = document.createElement("qty-picker");
  element.setAttribute("item-id", "DC-1042");
  for (const [name, value] of Object.entries(attributes)) {
    element.setAttribute(name, value);
  }
  parent.append(element);
  return element;
}

function press(element, selector, times = 1) {
  for (let i = 0; i < times; i++) {
    element.shadowRoot.querySelector(selector).click();
  }
}

const qtyText = (element) => element.shadowRoot.querySelector(".qty").textContent;

/** Listens on document for add-to-cart until the test ends, and returns the events heard. */
function hearOnDocument() {
  const heard = [];
  const listener = (event) => heard.push(event);
  document.addEventListener("add-to-cart", listener);
  cleanups.push(() => document.removeEventListener("add-to-cart", listener));
  return heard;
}

describe("<qty-picker>", () => {
  it("raises the quantity with + and lowers it with −", () => {
    const element = picker();
    press(element, "[data-action=\"inc\"]", 2);
    expect(qtyText(element), "two presses of + from 1 should show 3").toBe("3");
    press(element, "[data-action=\"dec\"]");
    expect(qtyText(element), "one press of − from 3 should show 2").toBe("2");
  });

  it("never goes below 1", () => {
    const element = picker();
    press(element, "[data-action=\"dec\"]", 3);
    expect(qtyText(element), "− at 1 should stay at 1").toBe("1");
  });

  it("never goes above the max attribute", () => {
    const element = picker({ max: "3" });
    press(element, "[data-action=\"inc\"]", 5);
    expect(qtyText(element), "with max=\"3\", + should stop at 3").toBe("3");
  });

  it("stops at 10 when there is no max", () => {
    const element = picker();
    press(element, "[data-action=\"inc\"]", 12);
    expect(qtyText(element), "with no max attribute, + should stop at 10").toBe("10");
  });

  it("sends add-to-cart with the item id and the quantity", () => {
    const element = picker();
    const details = [];
    element.addEventListener("add-to-cart", (event) => details.push(event.detail));
    press(element, "[data-action=\"inc\"]");
    press(element, ".add");
    expect(details, "Add with 2 chosen should send detail { id: \"DC-1042\", qty: 2 } once").toEqual([{ id: "DC-1042", qty: 2 }]);
  });

  it("sends a CustomEvent that bubbles up to the page", () => {
    const heard = hearOnDocument();
    const element = picker();
    press(element, ".add");
    expect(heard.length, "a listener on document should hear add-to-cart once: is bubbles true?").toBe(1);
    expect(heard[0] instanceof CustomEvent, "the event should be a CustomEvent").toBe(true);
    expect(heard[0]?.target, "outside the picker, event.target should be the picker itself").toBe(element);
  });

  it("reaches the page even from inside another component's shadow root", () => {
    const heard = hearOnDocument();
    const host = document.createElement("div");
    document.body.append(host);
    const element = picker({}, host.attachShadow({ mode: "open" }));
    press(element, ".add");
    expect(heard.length, "add-to-cart should cross the outer shadow root to reach document: is composed true?").toBe(1);
    expect(heard[0]?.detail, "the detail should arrive intact").toEqual({ id: "DC-1042", qty: 1 });
    expect(heard[0]?.target, "outside, event.target should be the outer host, not the picker inside it").toBe(host);
  });

  it("goes back to 1 after Add", () => {
    const element = picker();
    press(element, "[data-action=\"inc\"]", 3);
    press(element, ".add");
    expect(qtyText(element), "after Add, the quantity should go back to 1").toBe("1");
    expect(element.qty, "the qty property should be 1 again too").toBe(1);
  });
});

describe("listenForAddToCart", () => {
  it("calls onAdd with the id and quantity for a picker inside root", () => {
    document.body.innerHTML = `<ul id="catalogue"><li></li></ul>`;
    const onAdd = vi.fn();
    listenForAddToCart(document.querySelector("#catalogue"), onAdd);
    const element = picker({}, document.querySelector("#catalogue li"));
    press(element, "[data-action=\"inc\"]");
    press(element, ".add");
    expect(onAdd.mock.calls, "one Add with 2 chosen should call onAdd(\"DC-1042\", 2) once").toEqual([["DC-1042", 2]]);
  });

  it("ignores pickers outside root", () => {
    document.body.innerHTML = `<ul id="catalogue"></ul><aside id="guide"></aside>`;
    const onAdd = vi.fn();
    listenForAddToCart(document.querySelector("#catalogue"), onAdd);
    press(picker({}, document.querySelector("#guide")), ".add");
    expect(onAdd.mock.calls.length, "a picker in the gift guide is outside the catalogue and should not call onAdd").toBe(0);
  });
});
