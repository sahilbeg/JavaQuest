import { describe, it, expect } from "vitest";
import { page, source } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

describe("The welcome page", () => {
  it("tells browsers and screen readers that the page is in English", () => {
    const lang = page().documentElement.getAttribute("lang");
    expect(lang, 'the <html> element should have the attribute lang="en"').toBe("en");
  });

  it("puts the tagline in a paragraph with the class tagline", () => {
    const tagline = page().querySelector("p.tagline");
    expect(tagline, 'there should be a <p class="tagline"> element').not.toBeNull();
    expect(text(tagline), "the tagline paragraph should hold the line about Pune").toBe(
      "Handmade crochet from Pune, one stitch at a time.",
    );
  });

  it("draws a line with an hr element, which has no closing tag", () => {
    expect(page().querySelector("body hr"), "there should be an <hr> element in the body").not.toBeNull();
    expect(/<\/hr\s*>/i.test(source()), "<hr> is a void element, so remove any </hr> closing tag").toBe(false);
  });

  it("closes strong before it closes the paragraph", () => {
    const strong = page().querySelector("p.note strong");
    expect(strong, "the note paragraph should still have its <strong> element").not.toBeNull();
    expect(text(strong), "the <strong> element should hold the words Meera Aunty").toBe("Meera Aunty");
    expect(
      /<\/p>\s*<\/strong>/i.test(source()),
      "</strong> comes after </p>: close strong first, because the element opened last closes first",
    ).toBe(false);
  });
});
