import { describe, it, expect } from "vitest";
import { orderTotals } from "../src/lib/pricing.js";

describe("Stage 1: orderTotals", () => {
  it("charges nothing at all for an empty cart", () => {
    expect(orderTotals([]), "an empty cart has no subtotal, no offer and no delivery").toEqual({ subtotal: 0, offer: 0, delivery: 0, total: 0 });
  });

  it("adds ₹60 delivery to one Golu", () => {
    expect(orderTotals([{ paise: 45000, qty: 1 }]), "45000 + 6000 delivery = 51000 (TODO 1)").toEqual({ subtotal: 45000, offer: 0, delivery: 6000, total: 51000 });
  });

  it("still charges delivery at exactly ₹999, and not above it", () => {
    expect(orderTotals([{ paise: 99900, qty: 1 }]), "exactly 99900 is not above 99900, so delivery is 6000 (TODO 1)").toEqual({
      subtotal: 99900,
      offer: 0,
      delivery: 6000,
      total: 105900,
    });
    expect(orderTotals([{ paise: 45000, qty: 2 }, { paise: 15000, qty: 1 }]), "105000 is above 99900, so delivery is free (TODO 1)").toEqual({
      subtotal: 105000,
      offer: 0,
      delivery: 0,
      total: 105000,
    });
  });

  it("gives no Diwali offer at exactly ₹1,500", () => {
    expect(orderTotals([{ paise: 15000, qty: 10 }]), "ten coaster sets are exactly 150000, which is not above it: no offer (TODO 1)").toEqual({
      subtotal: 150000,
      offer: 0,
      delivery: 0,
      total: 150000,
    });
  });

  it("takes 10% off above ₹1,500, rounded down to whole paise", () => {
    expect(orderTotals([{ paise: 89900, qty: 1 }, { paise: 45000, qty: 2 }]), "179900 gets 17990 off, then free delivery (TODO 1)").toEqual({
      subtotal: 179900,
      offer: 17990,
      delivery: 0,
      total: 161910,
    });
    expect(orderTotals([{ paise: 150005, qty: 1 }]), "10% of 150005 is 15000.5, rounded down to 15000 (TODO 1)").toEqual({
      subtotal: 150005,
      offer: 15000,
      delivery: 0,
      total: 135005,
    });
  });
});
