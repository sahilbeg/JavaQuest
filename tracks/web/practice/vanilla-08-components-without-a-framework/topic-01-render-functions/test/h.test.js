import { describe, it, expect, vi } from "vitest";
import { h } from "../src/h.js";

describe("h", () => {
  it("makes an element with the tag it is given", () => {
    const element = h("section", null);
    expect(element.tagName, "h(\"section\", null) should make a <section>").toBe("SECTION");
  });

  it("puts the class prop into className", () => {
    const element = h("li", { class: "product sold-out" });
    expect(element.className, "{ class: \"product sold-out\" } should give className \"product sold-out\"").toBe("product sold-out");
  });

  it("copies the dataset prop into data- attributes", () => {
    const element = h("li", { dataset: { id: "DC-1042", colour: "Indigo" } });
    expect(element.getAttribute("data-id"), "{ dataset: { id: \"DC-1042\" } } should give data-id=\"DC-1042\"").toBe("DC-1042");
    expect(element.getAttribute("data-colour"), "every key of dataset should become a data- attribute").toBe("Indigo");
  });

  it("sets other props as attributes", () => {
    const element = h("button", { type: "button", "aria-label": "Add Golu the elephant" });
    expect(element.getAttribute("type"), "{ type: \"button\" } should set type=\"button\"").toBe("button");
    expect(element.getAttribute("aria-label"), "an aria-label prop should become the aria-label attribute").toBe("Add Golu the elephant");
  });

  it("treats true as an empty attribute and skips false, null and undefined", () => {
    const on = h("button", { disabled: true });
    const off = h("button", { disabled: false, title: null, name: undefined });
    expect(on.disabled, "{ disabled: true } should make a disabled button").toBe(true);
    expect(off.disabled, "{ disabled: false } should leave the button enabled").toBe(false);
    expect(off.hasAttribute("title"), "a null prop should not become an attribute").toBe(false);
    expect(off.hasAttribute("name"), "an undefined prop should not become an attribute").toBe(false);
  });

  it("adds a click listener for onClick", () => {
    const onClick = vi.fn();
    const element = h("button", { onClick });
    element.click();
    expect(onClick.mock.calls.length, "pressing the button should call the onClick function once").toBe(1);
    expect(element.hasAttribute("onclick"), "onClick should become a listener, not an attribute").toBe(false);
  });

  it("adds text children as plain text, never as HTML", () => {
    const element = h("p", null, "<b>Golu</b> & friends");
    expect(element.textContent, "the text should appear exactly as written").toBe("<b>Golu</b> & friends");
    expect(element.querySelector("b"), "text children must never turn into HTML elements").toBeNull();
  });

  it("adds element children and numbers in order", () => {
    const element = h("p", null, h("strong", null, "Stock:"), " ", 4);
    expect(element.firstElementChild?.tagName, "the <strong> child should be inside the <p>").toBe("STRONG");
    expect(element.textContent, "children should be added in order, with the number as text").toBe("Stock: 4");
  });

  it("flattens arrays and skips null, undefined and false", () => {
    const element = h("ul", null, [h("li", null, "Golu"), [h("li", null, "Coasters")]], null, false, undefined);
    expect(element.children.length, "two <li> children should be added from the nested arrays").toBe(2);
    expect(element.textContent, "null, undefined and false should add nothing").toBe("GoluCoasters");
  });
});
