import { describe, it, expect } from "vitest";
import { page } from "./html.js";

/** An element's text with runs of spaces and new lines squeezed to one space. */
const text = (el) => el.textContent.replace(/\s+/g, " ").trim();

describe("Links and Golu's photo", () => {
  it("has a link at the top that jumps to the Catalogue heading", () => {
    const doc = page();
    const jump = doc.querySelector('a[href="#catalogue"]');
    expect(jump, 'there should be a link with href="#catalogue"').not.toBeNull();
    expect(text(jump), "the jump link should say See the catalogue").toBe("See the catalogue");
    const target = doc.getElementById("catalogue");
    expect(target, 'some element needs id="catalogue", or the link has nowhere to land').not.toBeNull();
    expect(text(target), 'the element with id="catalogue" should be the Catalogue heading').toBe("Catalogue");
  });

  it("shows Golu's photo with alt text that describes him", () => {
    const img = page().querySelector('img[src="images/golu.jpg"]');
    expect(img, 'there should be an <img> with src="images/golu.jpg"').not.toBeNull();
    const alt = (img.getAttribute("alt") ?? "").trim();
    expect(alt.length > 0, "the photo needs an alt attribute that describes it").toBe(true);
    expect(/elephant/i.test(alt), "the alt text should say that Golu is an elephant").toBe(true);
    expect(/\.(jpg|jpeg|png)/i.test(alt), "the alt text should describe the picture, not repeat the file name").toBe(false);
    expect(/^(image|picture|photo) of/i.test(alt), 'skip "image of" or "photo of": screen readers already say it is an image').toBe(false);
  });

  it("gives the photo a width and a height of 300", () => {
    const img = page().querySelector('img[src="images/golu.jpg"]');
    expect(img, 'there should be an <img> with src="images/golu.jpg"').not.toBeNull();
    expect(img.getAttribute("width"), 'the photo should have width="300"').toBe("300");
    expect(img.getAttribute("height"), 'the photo should have height="300"').toBe("300");
  });

  it("links to Golu's own page with a relative URL", () => {
    const link = page().querySelector('a[href="items/dc-1042.html"]');
    expect(link, 'there should be a link with href="items/dc-1042.html"').not.toBeNull();
    expect(text(link), "the link to Golu's page should say Meet Golu").toBe("Meet Golu");
  });

  it("links to Dhaaga's WhatsApp with a full, absolute URL", () => {
    const link = page().querySelector('a[href="https://wa.me/919876543210"]');
    expect(link, 'there should be a link with href="https://wa.me/919876543210"').not.toBeNull();
    expect(text(link), "the WhatsApp link should say Order on WhatsApp").toBe("Order on WhatsApp");
  });
});
