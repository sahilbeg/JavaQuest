import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

describe("The catalogue page for screen reader users", () => {
  it("hides the decorative yarn border with an empty alt", () => {
    const border = page().querySelector('img[src="images/yarn-border.png"]');
    expect(border, "the yarn border image should still be on the page").not.toBeNull();
    expect(border.hasAttribute("alt"), 'the border needs an alt attribute: with none, screen readers read the file name').toBe(true);
    expect(border.getAttribute("alt"), 'the border is only decoration, so its alt should be empty: alt=""').toBe("");
  });

  it("gives the story link text that makes sense on its own", () => {
    const link = page().querySelector('a[href="story.html"]');
    expect(link, 'the link to story.html should still be on the page').not.toBeNull();
    const words = text(link);
    expect(/^(click here|here|read more|more|link)$/i.test(words), `"${words}" says nothing on its own: say where the link goes`).toBe(false);
    expect(/meera aunty/i.test(words), "the link's own text should mention Meera Aunty, like Read how Meera Aunty started").toBe(true);
  });

  it("marks the Hindi word with its own language", () => {
    const hindi = page().querySelector('[lang="hi"]');
    expect(hindi, 'the word धागा should be inside an element with lang="hi"').not.toBeNull();
    expect(text(hindi), 'the lang="hi" element should hold just the word धागा').toBe("धागा");
  });

  it("names the WhatsApp icon link by where it goes", () => {
    const icon = page().querySelector('footer a[href="https://wa.me/919876543210"] img');
    expect(icon, "the footer should still have the WhatsApp icon inside its link").not.toBeNull();
    const alt = (icon.getAttribute("alt") ?? "").trim();
    expect(alt.length > 0, "the icon is the only thing in its link, so it needs alt text, or the link has no name").toBe(true);
    expect(/whatsapp/i.test(alt), "the icon's alt should say where the link goes, like Chat with us on WhatsApp").toBe(true);
    expect(/\.png/i.test(alt), "the alt text should not be the file name").toBe(false);
  });

  it("gives every image on the page an alt attribute", () => {
    for (const img of page().querySelectorAll("img")) {
      expect(img.hasAttribute("alt"), `the image ${img.getAttribute("src")} has no alt attribute`).toBe(true);
    }
  });
});
