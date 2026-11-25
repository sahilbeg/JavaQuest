import { describe, it, expect } from "vitest";
import { checkoutTotals } from "../src/totals.ts";

const COASTERS = { code: "CST", name: "Coaster set", paise: 15000, qty: 2 };
const GOLU = { code: "AMI", name: "Golu the elephant", paise: 45000, qty: 1 };
const TOTE = { code: "BAG", name: "Tote bag", paise: 89900, qty: 1 };

describe("checkoutTotals", () => {
  it("adds the items and ₹60 delivery for a small order", () => {
    expect(checkoutTotals([COASTERS, GOLU]), "two coaster sets and Golu: 75000 + 6000 delivery").toEqual({
      items: 75000,
      delivery: 6000,
      total: 81000,
    });
  });

  it("delivers free above ₹999", () => {
    expect(checkoutTotals([TOTE, COASTERS]), "₹1,199 of items is above ₹999, so delivery is free").toEqual({
      items: 119900,
      delivery: 0,
      total: 119900,
    });
  });

  it("still charges delivery at exactly ₹999", () => {
    const line = { code: "X", name: "Exactly ₹999", paise: 99900, qty: 1 };
    expect(checkoutTotals([line]).delivery, "free delivery is only above ₹999, so ₹999 pays 6000").toBe(6000);
  });

  it("charges nothing for an empty cart", () => {
    expect(checkoutTotals([]), "an empty cart has no items and no delivery").toEqual({ items: 0, delivery: 0, total: 0 });
  });
});
