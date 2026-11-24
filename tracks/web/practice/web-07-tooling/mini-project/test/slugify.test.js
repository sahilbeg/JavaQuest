import { describe, it, expect } from "vitest";
import { slugify } from "../src/text.js";

describe("slugify", () => {
  it("turns a product name into small letters and dashes", () => {
    expect(slugify("Golu the Elephant!"), "\"Golu the Elephant!\" should become golu-the-elephant").toBe(
      "golu-the-elephant",
    );
  });

  it("keeps digits and squeezes every run of other characters into one dash", () => {
    expect(slugify("Rani pink -- coaster set (4)"), "brackets and repeated dashes become single dashes").toBe(
      "rani-pink-coaster-set-4",
    );
  });

  it("has no dash at the start or the end", () => {
    expect(slugify("  Tote bag, Indigo  "), "spaces at the ends should not leave dashes").toBe("tote-bag-indigo");
  });

  it("removes accents", () => {
    expect(slugify("Café Tote"), "é should become e").toBe("cafe-tote");
  });

  it("gives an empty slug when there are no letters or digits", () => {
    expect(slugify("!!!"), "nothing usable is left in \"!!!\"").toBe("");
  });
});
