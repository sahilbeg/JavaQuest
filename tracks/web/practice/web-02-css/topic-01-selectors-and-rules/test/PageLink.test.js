import { describe, it, expect } from "vitest";
import { page } from "./html.js";

describe("The catalogue page", () => {
  it("links style.css from its head", () => {
    const link = page().querySelector('head link[rel="stylesheet"]');
    expect(link, 'the <head> should have a <link rel="stylesheet" href="style.css">').not.toBeNull();
    expect(link.getAttribute("href"), "the link's href should be exactly style.css").toBe("style.css");
  });
});
