import { describe, it, expect } from "vitest";
import { page } from "./html.js";

describe("The page on a phone", () => {
  it("tells phones to use their real width", () => {
    const meta = page().querySelector('head meta[name="viewport"]');
    expect(meta, 'the <head> should have <meta name="viewport" content="width=device-width, initial-scale=1">').not.toBeNull();
    const content = (meta.getAttribute("content") ?? "").replace(/\s+/g, "");
    expect(content.includes("width=device-width"), "the viewport content should include width=device-width").toBe(true);
    expect(content.includes("initial-scale=1"), "the viewport content should include initial-scale=1").toBe(true);
  });
});
