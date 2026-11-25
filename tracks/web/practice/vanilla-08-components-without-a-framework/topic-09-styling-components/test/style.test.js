import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

describe("style.css", () => {
  it("defines the brand colours as tokens on :root", () => {
    expect(value(":root", "--dhaaga-marigold"), ":root should set --dhaaga-marigold: #F2A81D").toBe("#f2a81d");
    expect(value(":root", "--dhaaga-indigo"), ":root should set --dhaaga-indigo: #2E3A8C").toBe("#2e3a8c");
    expect(value(":root", "--dhaaga-off-white"), ":root should set --dhaaga-off-white: #FAF7F0").toBe("#faf7f0");
  });

  it("names every rule after the product-card block", () => {
    const strays = rules().map((rule) => rule.selector).filter((selector) => selector !== ":root" && !selector.startsWith(".product-card"));
    expect(strays, "every selector except :root should start with .product-card; these don't").toEqual([]);
  });

  it("styles each part of the card with its BEM class", () => {
    expect(value(".product-card", "background"), ".product-card's background should be var(--dhaaga-off-white)").toBe("var(--dhaaga-off-white)");
    expect(value(".product-card__name", "color"), ".product-card__name's color should be var(--dhaaga-indigo)").toBe("var(--dhaaga-indigo)");
    expect(value(".product-card__price", "font-weight"), ".product-card__price should keep font-weight: 700").toBe("700");
    expect(value(".product-card__add", "background"), ".product-card__add's background should be var(--dhaaga-marigold)").toBe("var(--dhaaga-marigold)");
    expect(value(".product-card--sold-out", "opacity"), ".product-card--sold-out should keep opacity: 0.5").toBe("0.5");
  });

  it("types no colour codes outside :root", () => {
    const typed = [];
    for (const rule of rules()) {
      if (rule.selector === ":root") {
        continue;
      }
      for (const [property, text] of rule.decls) {
        if (/#[0-9a-f]{3,8}\b/.test(text)) {
          typed.push(`${rule.selector} { ${property}: ${text} }`);
        }
      }
    }
    expect(typed, "colours outside :root should use var(--dhaaga-...); these still type a hex code").toEqual([]);
  });
});
