import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

const PHOTOS = ["images/coasters.jpg", "images/golu.jpg", "images/tote-bag.jpg"];

describe("Silver: a real button and honest pictures", () => {
  it("places the order with a real submit button inside the form", () => {
    const form = page().querySelector('form[action="/orders"]');
    expect(form, 'the page should still have its <form action="/orders">').not.toBeNull();
    const button = form.querySelector("button");
    expect(button, "the form needs a real <button>, which the Tab key can reach").not.toBeNull();
    expect(button.type, 'the button should be type="submit", so it sends the form').toBe("submit");
    expect(text(button), "the button should say Place order").toBe("Place order");
  });

  it("leaves no clickable div or span pretending to be a button", () => {
    const fakes = page().querySelectorAll("div[onclick], span[onclick], div.button, span.button");
    expect(fakes.length, "remove the clickable div: the real <button> does its job now").toBe(0);
  });

  it("describes each product photo in its alt text", () => {
    const doc = page();
    const alts = [];
    for (const src of PHOTOS) {
      const img = doc.querySelector(`img[src="${src}"]`);
      expect(img, `the photo ${src} should still be on the page`).not.toBeNull();
      const alt = (img.getAttribute("alt") ?? "").trim();
      expect(alt.length > 0, `the photo ${src} shows a product, so it needs alt text that describes it`).toBe(true);
      expect(/\.(jpg|jpeg|png)/i.test(alt), `the alt text for ${src} should describe the product, not repeat the file name`).toBe(false);
      expect(/^(image|picture|photo)$/i.test(alt), `"${alt}" says nothing about ${src}: describe the product`).toBe(false);
      alts.push(alt.toLowerCase());
    }
    expect(new Set(alts).size, "each of the three photos shows a different product, so each needs its own alt text").toBe(3);
  });

  it("hides the decorative stitched line with an empty alt", () => {
    const line = page().querySelector('img[src="images/stitch-line.png"]');
    expect(line, "the stitched line image should still be on the page").not.toBeNull();
    expect(line.hasAttribute("alt"), "the stitched line needs an alt attribute, or screen readers read its file name").toBe(true);
    expect(line.getAttribute("alt"), 'the stitched line is only decoration, so its alt should be empty: alt=""').toBe("");
  });
});
