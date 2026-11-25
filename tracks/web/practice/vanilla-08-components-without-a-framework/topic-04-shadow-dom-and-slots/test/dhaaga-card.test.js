import { describe, it, expect, afterEach } from "vitest";
import { DhaagaCard } from "../src/dhaaga-card.js";

afterEach(() => {
  document.body.innerHTML = "";
});

/** A card with a title and a blurb, and optionally a price and the sale attribute, on the page. */
function card({ sale = false, price = null } = {}) {
  const element = document.createElement("dhaaga-card");
  if (sale) {
    element.setAttribute("sale", "");
  }
  const title = document.createElement("h3");
  title.setAttribute("slot", "title");
  title.textContent = "Golu the elephant";
  const blurb = document.createElement("p");
  blurb.textContent = "Crocheted in Indigo, about 15 cm tall.";
  element.append(title, blurb);
  if (price !== null) {
    const tag = document.createElement("p");
    tag.setAttribute("slot", "price");
    tag.textContent = price;
    element.append(tag);
  }
  document.body.append(element);
  return element;
}

const slotNamed = (element, name) => element.shadowRoot?.querySelector(`slot[name="${name}"]`);
const defaultSlot = (element) => [...(element.shadowRoot?.querySelectorAll("slot") ?? [])].find((slot) => !slot.hasAttribute("name"));

describe("<dhaaga-card>", () => {
  it("has an open shadow root", () => {
    const element = card();
    expect(element instanceof DhaagaCard, "a <dhaaga-card> should be a DhaagaCard").toBe(true);
    expect(element.shadowRoot, "the card should have a shadow root: call attachShadow in the constructor").not.toBeNull();
    expect(element.shadowRoot?.mode, "the shadow root should be open").toBe("open");
  });

  it("keeps its article inside the shadow root, away from the page", () => {
    const element = card();
    const article = element.shadowRoot?.querySelector("article");
    expect(article, "the shadow root should hold an <article>").not.toBeUndefined();
    expect(article?.getAttribute("part"), "the article should have part=\"card\"").toBe("card");
    expect(document.querySelector("article"), "the page's own searches must not find the card's article").toBeNull();
  });

  it("has its own style with a :host rule", () => {
    const element = card();
    const style = element.shadowRoot?.querySelector("style");
    expect(style?.textContent ?? "", "the shadow root should have a <style> with a :host rule").toContain(":host");
    expect(style?.textContent ?? "", "the :host rule should set display: block").toMatch(/:host\s*\{[^}]*display:\s*block/);
  });

  it("shows the title through the title slot, inside a header", () => {
    const element = card();
    const slot = slotNamed(element, "title");
    expect(slot?.parentElement?.tagName, "<slot name=\"title\"> should sit inside a <header>").toBe("HEADER");
    expect(slot?.assignedElements().map((e) => e.textContent), "the h3 with slot=\"title\" should fill the title slot").toEqual(["Golu the elephant"]);
    expect(element.querySelector("h3")?.textContent, "the slotted h3 should still be the card's own child").toBe("Golu the elephant");
  });

  it("shows other children through the default slot", () => {
    const element = card();
    const slot = defaultSlot(element);
    expect(slot, "the shadow root should have a <slot> with no name").not.toBeUndefined();
    expect(slot?.assignedElements().map((e) => e.textContent), "the p with no slot attribute should fill the default slot").toEqual(["Crocheted in Indigo, about 15 cm tall."]);
  });

  it("shows Price on request when the page gives no price", () => {
    const element = card();
    const slot = slotNamed(element, "price");
    expect(slot?.parentElement?.tagName, "<slot name=\"price\"> should sit inside a <footer>").toBe("FOOTER");
    expect(slot?.textContent, "the price slot's fallback text should be Price on request").toBe("Price on request");
    expect(slot?.assignedNodes().length, "with no price given, nothing should fill the price slot").toBe(0);
  });

  it("puts a given price into the price slot", () => {
    const element = card({ price: "₹450.00" });
    const slot = slotNamed(element, "price");
    expect(slot?.assignedElements().map((e) => e.textContent), "the p with slot=\"price\" should fill the price slot").toEqual(["₹450.00"]);
  });

  it("shows the Diwali badge only while the card has a sale attribute", () => {
    const plain = card();
    expect(plain.shadowRoot?.querySelector(".badge")?.hidden, "a card without sale should keep its badge hidden").toBe(true);
    const onSale = card({ sale: true });
    expect(onSale.shadowRoot?.querySelector(".badge")?.hidden, "a card with sale should show its badge").toBe(false);
    onSale.removeAttribute("sale");
    expect(onSale.shadowRoot?.querySelector(".badge")?.hidden, "removing sale should hide the badge again").toBe(true);
  });

  it("can be moved on the page without breaking", () => {
    const element = card();
    const root = element.shadowRoot;
    expect(() => {
      element.remove();
      document.body.append(element);
    }, "moving the card should not throw: attach the shadow root only once").not.toThrow();
    expect(element.shadowRoot, "the card should keep the same shadow root after a move").toBe(root);
  });
});
