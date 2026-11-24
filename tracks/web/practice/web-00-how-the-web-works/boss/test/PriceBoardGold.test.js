import { describe, it, expect } from "vitest";
import { page } from "./html.js";

describe("Gold: ready for every phone and every reader", () => {
  it("says the page is in English with lang=\"en\" on the html element", () => {
    const lang = (page().documentElement.getAttribute("lang") ?? "").trim().toLowerCase();
    expect(lang, 'the <html> element should have lang="en"').toBe("en");
  });

  it("has a viewport line so phones use their real width", () => {
    const meta = page().querySelector('meta[name="viewport"]');
    expect(meta, 'add <meta name="viewport" content="width=device-width, initial-scale=1"> inside <head>').not.toBeNull();
    const content = (meta.getAttribute("content") ?? "").replace(/\s/g, "").toLowerCase();
    expect(content.includes("width=device-width"), "the viewport content should include width=device-width").toBe(true);
    expect(content.includes("initial-scale=1"), "the viewport content should include initial-scale=1").toBe(true);
  });
});
