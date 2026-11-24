import { describe, it, expect } from "vitest";
import { page } from "./html.js";

const PRICES = ["₹150", "₹120", "₹450", "₹899", "₹350"];

describe("Bronze: the ₹ sign shows properly", () => {
  it("tells the browser the page is UTF-8", () => {
    const meta = page().querySelector("meta[charset]");
    expect(meta, 'add <meta charset="utf-8"> inside <head>').not.toBeNull();
    expect(meta.getAttribute("charset").trim().toLowerCase(), 'the charset should be utf-8, with the dash').toBe("utf-8");
  });

  it("still shows every price with the ₹ sign", () => {
    const text = page().body.textContent;
    for (const price of PRICES) {
      expect(text.includes(price), `the price board should still show ${price}; keep the ₹ sign and fix the charset instead`).toBe(true);
    }
  });
});
