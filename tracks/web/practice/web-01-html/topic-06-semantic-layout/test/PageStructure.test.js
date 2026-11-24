import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

describe("The catalogue page's structure", () => {
  it("has one header, straight inside body, holding the h1", () => {
    const doc = page();
    const headers = doc.querySelectorAll("header");
    expect(headers.length, "the page should have exactly one <header>").toBe(1);
    expect(headers[0].parentElement, "the <header> should sit straight inside <body>").toBe(doc.body);
    expect(headers[0].querySelector("h1"), "the <h1> should be inside the <header>").not.toBeNull();
  });

  it("puts the menu in a nav inside the header, with its links in a list", () => {
    const doc = page();
    const nav = doc.querySelector("header nav");
    expect(nav, "there should be a <nav> inside the <header>").not.toBeNull();
    const links = Array.from(nav.querySelectorAll("ul > li > a"));
    expect(
      links.map((a) => a.getAttribute("href")),
      "the <nav> should hold a <ul> with three <li>, each with one link: #story, #catalogue, #prices",
    ).toEqual(["#story", "#catalogue", "#prices"]);
  });

  it("has one main, straight inside body, with a section for each part", () => {
    const doc = page();
    const mains = doc.querySelectorAll("main");
    expect(mains.length, "the page should have exactly one <main>").toBe(1);
    expect(mains[0].parentElement, "the <main> should sit straight inside <body>").toBe(doc.body);
    for (const id of ["story", "catalogue", "prices"]) {
      const part = doc.getElementById(id);
      expect(part?.tagName, `the element with id="${id}" should be a <section>`).toBe("SECTION");
      expect(mains[0].contains(part), `the ${id} section should be inside <main>`).toBe(true);
      expect(part.querySelector("h2"), `the ${id} section should keep its <h2> heading`).not.toBeNull();
    }
  });

  it("has one footer, straight inside body, with the shop's hours", () => {
    const doc = page();
    const footers = doc.querySelectorAll("footer");
    expect(footers.length, "the page should have exactly one <footer>").toBe(1);
    expect(footers[0].parentElement, "the <footer> should sit straight inside <body>, after <main>").toBe(doc.body);
    expect(text(footers[0]), "the <footer> should still hold the shop's hours").toContain("11:00 to 21:00");
  });
});
