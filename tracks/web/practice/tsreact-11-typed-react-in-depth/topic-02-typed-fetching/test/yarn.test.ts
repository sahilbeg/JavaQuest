import { describe, it, expect } from "vitest";
import { isYarnStock } from "../src/yarn.ts";

describe("isYarnStock", () => {
  it("accepts a good answer from the stock API", () => {
    expect(isYarnStock({ colour: "Marigold", grams: 1200 }), "{ colour: \"Marigold\", grams: 1200 } is a YarnStock").toBe(true);
  });

  it("accepts an answer with extra fields", () => {
    expect(isYarnStock({ colour: "Indigo", grams: 600, shelf: "B2" }), "an extra field doesn't matter, as long as colour and grams are right").toBe(true);
  });

  it("refuses grams sent as text", () => {
    expect(isYarnStock({ colour: "Mint", grams: "300" }), "grams must be a number, not the text \"300\"").toBe(false);
  });

  it("refuses an answer with a missing field", () => {
    expect(isYarnStock({ grams: 300 }), "an answer without colour is not a YarnStock").toBe(false);
    expect(isYarnStock({ colour: "Mint" }), "an answer without grams is not a YarnStock").toBe(false);
  });

  it("refuses null, text and arrays without crashing", () => {
    expect(isYarnStock(null), "null is not a YarnStock (remember typeof null is \"object\")").toBe(false);
    expect(isYarnStock("Marigold"), "a plain string is not a YarnStock").toBe(false);
    expect(isYarnStock([1200]), "an array is not a YarnStock").toBe(false);
  });
});
