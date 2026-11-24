import { describe, it, expect } from "vitest";
import { page } from "./html.js";

describe("The catalogue page's head", () => {
  it("puts the charset line first in the head", () => {
    const first = page().head.firstElementChild;
    expect(first?.tagName, "the first element inside <head> should be the <meta charset> line").toBe("META");
    expect((first.getAttribute("charset") ?? "").toLowerCase(), 'the first <meta> in the head should be <meta charset="utf-8">').toBe("utf-8");
  });

  it("names the page Catalogue | Dhaaga & Co.", () => {
    const doc = page();
    expect(doc.querySelectorAll("head title").length, "the head should have exactly one <title>").toBe(1);
    expect(doc.title, "the title should be Catalogue | Dhaaga & Co. (write the & as &amp;)").toBe("Catalogue | Dhaaga & Co.");
  });

  it("tells phones to use their real width", () => {
    const viewport = page().querySelector('head meta[name="viewport"]');
    expect(viewport, 'the head should have a <meta name="viewport"> tag').not.toBeNull();
    const content = (viewport.getAttribute("content") ?? "").replace(/\s+/g, "");
    expect(
      /^width=device-width,initial-scale=1(\.0)?$/.test(content),
      'the viewport content should be "width=device-width, initial-scale=1"',
    ).toBe(true);
  });

  it("has a short description that mentions crochet", () => {
    const meta = page().querySelector('head meta[name="description"]');
    expect(meta, 'the head should have a <meta name="description"> tag').not.toBeNull();
    const content = (meta.getAttribute("content") ?? "").trim();
    expect(content.length > 0, "the description's content should not be empty").toBe(true);
    expect(content.length <= 160, `the description is ${content.length} characters: keep it to 160 or fewer`).toBe(true);
    expect(/crochet/i.test(content), "the description should mention crochet, so people know what Dhaaga sells").toBe(true);
  });

  it("links Golu's picture as the tab icon", () => {
    const icon = page().querySelector('head link[rel="icon"]');
    expect(icon, 'the head should have a <link rel="icon"> tag').not.toBeNull();
    expect(icon.getAttribute("href"), 'the icon link should have href="images/golu-icon.png"').toBe("images/golu-icon.png");
  });
});
