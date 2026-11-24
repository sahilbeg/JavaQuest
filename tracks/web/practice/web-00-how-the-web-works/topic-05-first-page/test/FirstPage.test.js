import { describe, it, expect } from "vitest";
import { page } from "./html.js";

function paragraphs() {
  return [...page().querySelectorAll("p")].map((p) => p.textContent.toLowerCase());
}

describe("Dhaaga's first page", () => {
  it("has the title Dhaaga & Co. inside the head", () => {
    const title = page().head.querySelector("title");
    expect(title, "add a <title> element inside <head>").not.toBeNull();
    expect(title.textContent.trim(), "the <title> should say exactly Dhaaga & Co.").toBe("Dhaaga & Co.");
  });

  it("has exactly one main heading that says Dhaaga & Co.", () => {
    const headings = page().querySelectorAll("h1");
    expect(headings.length, "the page should have exactly one <h1>").toBe(1);
    expect(headings[0].textContent.trim(), "the <h1> should say exactly Dhaaga & Co.").toBe("Dhaaga & Co.");
  });

  it("has two paragraphs under the heading", () => {
    expect(paragraphs().length, "the page should have two <p> paragraphs").toBe(2);
  });

  it("says in a paragraph that Dhaaga makes crochet", () => {
    expect(paragraphs().some((text) => text.includes("crochet")), "one <p> should use the word crochet").toBe(true);
  });

  it("says in a paragraph that the stall is on FC Road", () => {
    expect(paragraphs().some((text) => text.includes("fc road")), "one <p> should mention FC Road").toBe(true);
  });
});
