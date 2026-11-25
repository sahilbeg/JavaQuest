import { describe, it, expect, afterEach } from "vitest";
import { ProductCard, stockText } from "../src/product-card.js";

const cleanups = [];

afterEach(() => {
  while (cleanups.length > 0) {
    cleanups.pop()();
  }
  document.body.innerHTML = "";
});

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 4 };

function card(product = golu) {
  const element = document.createElement("product-card");
  element.product = product;
  document.body.append(element);
  return element;
}

const inShadow = (element, selector) => element.shadowRoot?.querySelector(selector);

describe("stockText", () => {
  it("describes the stock in words", () => {
    expect(stockText(0), "0 pieces should be Sold out").toBe("Sold out");
    expect(stockText(4), "4 pieces should be Only 4 left").toBe("Only 4 left");
    expect(stockText(5), "5 pieces should be Only 5 left").toBe("Only 5 left");
    expect(stockText(6), "6 pieces should be In stock").toBe("In stock");
  });
});

describe("<product-card>", () => {
  it("is a ProductCard with an open shadow root", () => {
    const element = card();
    expect(element instanceof ProductCard, "a <product-card> should be a ProductCard").toBe(true);
    expect(element.shadowRoot?.mode, "the card should have an open shadow root").toBe("open");
    expect(inShadow(element, "style")?.textContent ?? "", "the shadow root should hold the card's <style>").toContain(":host");
  });

  it("draws its product: name, price and stock", () => {
    const element = card();
    expect(inShadow(element, ".name")?.textContent, ".name should say Golu the elephant").toBe("Golu the elephant");
    expect(inShadow(element, ".price")?.textContent, ".price should say ₹450.00").toBe("₹450.00");
    expect(inShadow(element, ".stock")?.textContent, ".stock should say Only 4 left").toBe("Only 4 left");
    expect(inShadow(element, ".add")?.disabled, "Add should be enabled with stock left").toBe(false);
  });

  it("gives its product back", () => {
    const element = card();
    expect(element.product, "card.product should return the product that was set").toBe(golu);
  });

  it("draws again when its product is set again", () => {
    const element = card();
    element.product = { ...golu, stock: 0 };
    expect(inShadow(element, ".stock")?.textContent, "after the stock drops to 0, .stock should say Sold out").toBe("Sold out");
    expect(inShadow(element, ".add")?.disabled, "a sold-out card's Add button should be disabled").toBe(true);
  });

  it("sends add-to-cart with its id, out to the page", () => {
    const heard = [];
    const listener = (event) => heard.push(event);
    document.addEventListener("add-to-cart", listener);
    cleanups.push(() => document.removeEventListener("add-to-cart", listener));
    const element = card();
    inShadow(element, ".add")?.click();
    expect(heard.length, "pressing Add should send one add-to-cart that reaches document").toBe(1);
    expect(heard[0]?.detail, "the detail should be { id: \"DC-1042\" }").toEqual({ id: "DC-1042" });
    expect(heard[0]?.composed, "the event should be composed, so it can leave other shadow roots").toBe(true);
    expect(heard[0]?.target, "outside, event.target should be the card").toBe(element);
  });
});
