import { describe, it, expect, afterEach } from "vitest";
import { StockBadge } from "../src/stock-badge.js";

afterEach(() => {
  document.body.innerHTML = "";
});

/** Makes a badge the way a page would, sets its count, and puts it on the page. */
function badge(count) {
  const element = document.createElement("stock-badge");
  element.setAttribute("count", String(count));
  document.body.append(element);
  return element;
}

describe("<stock-badge>", () => {
  it("is registered as the stock-badge tag", () => {
    expect(customElements.get("stock-badge"), "customElements.define(\"stock-badge\", StockBadge) should register the class").toBe(StockBadge);
    expect(document.createElement("stock-badge") instanceof StockBadge, "a new <stock-badge> should be a StockBadge").toBe(true);
  });

  it("says Only 3 left for a count of 3", () => {
    const element = badge(3);
    expect(element.textContent, "a count of 3 should show Only 3 left").toBe("Only 3 left");
    expect(element.classList.contains("low"), "a count of 3 should add the class low").toBe(true);
    expect(element.classList.contains("out"), "a count of 3 should not add the class out").toBe(false);
  });

  it("says In stock above 5", () => {
    const element = badge(12);
    expect(element.textContent, "a count of 12 should show In stock").toBe("In stock");
    expect(element.classList.contains("low"), "a count of 12 should not have the class low").toBe(false);
  });

  it("says Sold out for 0", () => {
    const element = badge(0);
    expect(element.textContent, "a count of 0 should show Sold out").toBe("Sold out");
    expect(element.classList.contains("out"), "a count of 0 should add the class out").toBe(true);
  });

  it("draws again when the count attribute changes", () => {
    const element = badge(8);
    element.setAttribute("count", "2");
    expect(element.textContent, "after count becomes 2, the badge should show Only 2 left: is count in observedAttributes?").toBe("Only 2 left");
    element.setAttribute("count", "0");
    expect(element.textContent, "after count becomes 0, the badge should show Sold out").toBe("Sold out");
    expect(element.classList.contains("low"), "the old class low should go away at 0").toBe(false);
    expect(element.classList.contains("out"), "the class out should be added at 0").toBe(true);
  });

  it("has a count property that reads and writes the attribute", () => {
    const element = badge(4);
    expect(element.count, "badge.count should read the attribute as the number 4").toBe(4);
    element.count = 7;
    expect(element.getAttribute("count"), "badge.count = 7 should set the attribute to \"7\"").toBe("7");
    expect(element.textContent, "after badge.count = 7, the badge should show In stock").toBe("In stock");
  });

  it("draws itself when it joins the page", () => {
    const element = document.createElement("stock-badge");
    document.body.append(element);
    expect(element.textContent, "a badge with no count should draw as Sold out when it joins the page").toBe("Sold out");
  });
});
