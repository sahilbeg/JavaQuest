import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

describe("Gold: headings and landmarks a listener can skim", () => {
  it("has one h1 that says Place an order", () => {
    const h1s = page().querySelectorAll("h1");
    expect(h1s.length, "the page should have exactly one <h1>").toBe(1);
    expect(text(h1s[0]), "the <h1> should say Place an order").toBe("Place an order");
  });

  it("never skips a heading level on the way down", () => {
    const all = Array.from(page().querySelectorAll("h1, h2, h3, h4, h5, h6"));
    expect(all[0]?.tagName, "the first heading on the page should be the <h1>").toBe("H1");
    for (let i = 1; i < all.length; i++) {
      const before = Number(all[i - 1].tagName[1]);
      const now = Number(all[i].tagName[1]);
      expect(now <= before + 1, `"${text(all[i])}" is an <h${now}> right after an <h${before}>: go down one level at a time`).toBe(true);
    }
  });

  it("has a header with the menu in a nav inside it", () => {
    const doc = page();
    expect(doc.querySelectorAll("header").length, "the page should have exactly one <header>").toBe(1);
    const nav = doc.querySelector("header nav");
    expect(nav, "the menu of links should be a <nav> inside the <header>").not.toBeNull();
    expect(nav.querySelectorAll("a").length, "the <nav> should still hold the Catalogue and Order links").toBe(2);
  });

  it("has one main holding the h1 and the form, and a footer", () => {
    const doc = page();
    const mains = doc.querySelectorAll("main");
    expect(mains.length, "the page should have exactly one <main>").toBe(1);
    expect(mains[0].querySelector("h1"), "the <h1> should be inside <main>").not.toBeNull();
    expect(mains[0].querySelector('form[action="/orders"]'), "the order form should be inside <main>").not.toBeNull();
    const footers = doc.querySelectorAll("footer");
    expect(footers.length, "the page should have exactly one <footer>").toBe(1);
    expect(mains[0].contains(footers[0]), "the <footer> should come after <main>, not inside it").toBe(false);
  });
});
