import { describe, it, expect } from "vitest";
import { cases } from "../src/deliveryCases.js";
import { deliveryFee } from "../src/delivery.js";
import { correctFee, brokenVersions } from "./brokenVersions.js";

/** The cases whose expected fee differs from what this version of the fee gives. */
function casesThatFail(fee) {
  return cases.filter((c) => fee(c.subtotal) !== c.expected);
}

describe("your table of cases", () => {
  it("has at least four cases", () => {
    expect(cases.length, "write at least four cases in src/deliveryCases.js").toBeGreaterThanOrEqual(4);
  });

  it("gives every case a name, a whole-paise subtotal and an expected fee", () => {
    for (const c of cases) {
      const named = typeof c.name === "string" && c.name.trim() !== "";
      expect(named, `every case needs a name sentence; this one has none: ${JSON.stringify(c)}`).toBe(true);
      const wholeSubtotal = Number.isInteger(c.subtotal) && c.subtotal >= 0;
      expect(wholeSubtotal, `"${c.name}" needs a subtotal in whole paise, 0 or more`).toBe(true);
      expect(Number.isInteger(c.expected), `"${c.name}" needs an expected fee in whole paise`).toBe(true);
    }
  });

  it("expects the right fee in every case", () => {
    for (const c of cases) {
      expect(c.expected, `"${c.name}": by the rule, ${c.subtotal} paise pays ${correctFee(c.subtotal)} paise delivery`).toBe(
        correctFee(c.subtotal),
      );
    }
  });

  it("passes every case on your own deliveryFee", () => {
    for (const c of cases) {
      if (!Number.isInteger(c.subtotal) || c.subtotal < 0) continue;
      expect(deliveryFee(c.subtotal), `"${c.name}" fails on your deliveryFee in src/delivery.js`).toBe(c.expected);
    }
  });
});

describe("your cases against the broken versions", () => {
  for (const broken of brokenVersions) {
    it(`catches the version that ${broken.description}`, () => {
      expect(
        casesThatFail(broken.fee).length,
        `none of your cases fails on the version that ${broken.description}. Add a case that would.`,
      ).toBeGreaterThan(0);
    });
  }
});
