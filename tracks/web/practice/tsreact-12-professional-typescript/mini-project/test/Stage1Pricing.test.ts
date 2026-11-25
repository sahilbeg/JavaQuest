import { describe, it, expect } from "vitest";
import { cartTotals } from "../src/pricing.ts";

const line = (id: string, paise: number, qty: number) => ({ id, name: id, paise, qty });
const golu = (qty: number) => ({ id: "DC-1042", name: "Golu the elephant", paise: 45000, qty });
const tote = (qty: number) => ({ id: "DC-1301", name: "Rani pink tote bag", paise: 89900, qty });

describe("Stage 1: cartTotals", () => {
  it("is all zeros for an empty cart, with no delivery", () => {
    expect(cartTotals([]), "an empty cart costs nothing, not even delivery").toEqual({ subtotal: 0, discount: 0, delivery: 0, total: 0 });
  });

  it("adds ₹60 delivery to a small order", () => {
    expect(cartTotals([golu(1)]), "one Golu is 45000 paise, plus 6000 delivery: 51000 (TODO 1)").toEqual({
      subtotal: 45000,
      discount: 0,
      delivery: 6000,
      total: 51000,
    });
  });

  it("multiplies each price by its quantity", () => {
    expect(cartTotals([golu(2), line("DC-1201", 12000, 1)]).subtotal, "2 × 45000 + 12000 = 102000 paise").toBe(102000);
  });

  it("makes delivery free only above ₹999", () => {
    expect(cartTotals([line("gift-box", 99900, 1)]).delivery, "exactly ₹999 is not above ₹999, so delivery is still 6000 paise").toBe(6000);
    expect(cartTotals([line("gift-box", 99901, 1)]).delivery, "₹999.01 is above ₹999, so delivery is free").toBe(0);
  });

  it("gives the Diwali offer only above ₹1,500", () => {
    const tenCoasters = cartTotals([line("DC-1101", 15000, 10)]);
    expect(tenCoasters.discount, "exactly ₹1,500 is not above ₹1,500, so there is no offer").toBe(0);
    expect(tenCoasters.total, "₹1,500 with free delivery and no offer is 150000 paise").toBe(150000);
  });

  it("takes 10% off a big order, with free delivery", () => {
    expect(cartTotals([tote(2)]), "two totes are 179800; 10% off is 17980; delivery is free; the total is 161820").toEqual({
      subtotal: 179800,
      discount: 17980,
      delivery: 0,
      total: 161820,
    });
  });

  it("rounds the offer down to whole paise", () => {
    expect(cartTotals([line("hamper", 150009, 1)]).discount, "10% of 150009 is 15000.9, rounded down to 15000").toBe(15000);
  });

  it("leaves the lines it was given unchanged", () => {
    const lines = [golu(2)];
    cartTotals(lines);
    expect(lines, "cartTotals only reads the lines").toEqual([golu(2)]);
  });
});
