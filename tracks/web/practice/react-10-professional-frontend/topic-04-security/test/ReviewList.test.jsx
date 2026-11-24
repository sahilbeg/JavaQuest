import { describe, it, expect, afterEach } from "vitest";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { render, screen, cleanup } from "@testing-library/react";
import { ReviewList } from "../src/ReviewList.jsx";

afterEach(cleanup);

const ATTACK = 'Lovely coasters! <img src="x" onerror="alert(1)"> <script>alert(2)</script>';

const REVIEWS = [
  { id: "r1", author: "Anjali", rating: 5, text: "Golu is even cuter in real life!", website: "https://instagram.com/anjali.makes" },
  { id: "r2", author: "<b>Rohan</b>", rating: 4, text: ATTACK, website: "javascript:alert(document.cookie)" },
  { id: "r3", author: "Farah", rating: 3, text: "The booties were a little big for my niece." },
];

describe("ReviewList", () => {
  it("shows a review's words as plain text, tags and all", () => {
    render(<ReviewList reviews={REVIEWS} />);
    expect(document.querySelectorAll(".reviews img").length, "a review's words must never become HTML: Rohan's review made an <img> with an onerror attack").toBe(0);
    expect(document.querySelectorAll(".reviews script").length, "a review's words must never become HTML: Rohan's review made a <script>").toBe(0);
    expect(screen.queryByText(ATTACK), "Rohan's review should appear exactly as he typed it, angle brackets and all").not.toBeNull();
  });

  it("shows the author's name as plain text", () => {
    render(<ReviewList reviews={REVIEWS} />);
    expect(document.querySelectorAll(".reviews b").length, "an author called <b>Rohan</b> must not make bold HTML").toBe(0);
    expect(screen.queryByText("<b>Rohan</b>"), "the author's name should show exactly as typed: <b>Rohan</b>").not.toBeNull();
  });

  it("links a reviewer's https website safely", () => {
    render(<ReviewList reviews={REVIEWS} />);
    const link = screen.queryByRole("link", { name: "Anjali" });
    expect(link, "Anjali's name should link to her https website").not.toBeNull();
    expect(link.getAttribute("href"), "the link should go to https://instagram.com/anjali.makes").toBe("https://instagram.com/anjali.makes");
    expect(link.getAttribute("target"), "a customer's website should open in a new tab: target=\"_blank\"").toBe("_blank");
    const rel = (link.getAttribute("rel") ?? "").split(/\s+/);
    for (const word of ["nofollow", "noopener", "noreferrer"]) {
      expect(rel.includes(word), `the link's rel should include ${word}: rel="nofollow noopener noreferrer"`).toBe(true);
    }
  });

  it("links only safe websites, and nothing else", () => {
    render(<ReviewList reviews={REVIEWS} />);
    const anchors = [...document.querySelectorAll(".reviews a")];
    expect(anchors.length, "only Anjali has a safe website. Rohan's javascript: link and Farah's missing website must not become <a> tags").toBe(1);
  });

  it("never uses dangerouslySetInnerHTML", () => {
    const source = readFileSync(join(process.cwd(), "src", "ReviewList.jsx"), "utf8");
    expect(source.includes("dangerouslySetInnerHTML"), "ReviewList.jsx should not use dangerouslySetInnerHTML at all: let React escape the text").toBe(false);
  });
});
