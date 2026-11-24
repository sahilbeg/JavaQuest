import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

/** Every heading on the page, in the order they appear. */
const headings = (doc) => Array.from(doc.querySelectorAll("h1, h2, h3, h4, h5, h6"));

describe("The catalogue page's text", () => {
  it("has one main heading that names the shop", () => {
    const h1s = page().querySelectorAll("h1");
    expect(h1s.length, "the page should have exactly one <h1>").toBe(1);
    expect(text(h1s[0]), "the <h1> should say just Dhaaga & Co.").toBe("Dhaaga & Co.");
  });

  it("gives the story part an h2 right above its paragraphs", () => {
    const story = headings(page()).find((h) => text(h) === "Our story");
    expect(story, "there should be a heading that says Our story").toBeDefined();
    expect(story.tagName, "Our story is a big part of the page, so it should be an <h2>").toBe("H2");
    expect(story.nextElementSibling?.tagName, "the Our story heading should come right before the first story paragraph").toBe("P");
  });

  it("never skips a heading level on the way down", () => {
    const all = headings(page());
    expect(all[0]?.tagName, "the first heading on the page should be the <h1>").toBe("H1");
    for (let i = 1; i < all.length; i++) {
      const before = Number(all[i - 1].tagName[1]);
      const now = Number(all[i].tagName[1]);
      expect(
        now <= before + 1,
        `"${text(all[i])}" is an <h${now}> right after an <h${before}>: go down one level at a time`,
      ).toBe(true);
    }
    const fresh = all.find((h) => text(h) === "New this week");
    expect(fresh?.tagName, "New this week sits inside the Catalogue part, so it should be an <h3>").toBe("H3");
  });

  it("shows the angle brackets in the stitching tip as text", () => {
    const tip = page().querySelector("p.stitch-tip");
    expect(tip, 'the <p class="stitch-tip"> paragraph should still be there').not.toBeNull();
    expect(text(tip), "the tip should show <your name>, so write the brackets as &lt; and &gt;").toBe(
      "Tip: write <your name> in the notes and we stitch it on for ₹50.",
    );
  });
});
