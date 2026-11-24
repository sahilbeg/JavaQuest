import { describe, it, expect } from "vitest";
import { cartTotals } from "../src/pricing.js";

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
const coasters = { id: "DC-1101", name: "Marigold coaster set", paise: 15000 };
const keychain = { id: "DC-1201", name: "Mint keychain", paise: 12000 };
const tote = { id: "DC-1301", name: "Rani pink tote bag", paise: 89900 };

describe("Stage 1: cartTotals", () => {
  it("gives all zeros for an empty cart, with no delivery charge", () => {
    expect(cartTotals([]), "an empty cart costs nothing, not even delivery").toEqual({ subtotal: 0, discount: 0, delivery: 0, total: 0 });
  });

  it("adds ₹60 delivery to a small order", () => {
    expect(cartTotals([{ ...golu, qty: 1 }]), "one Golu: 45000 + 6000 delivery = 51000").toEqual({ subtotal: 45000, discount: 0, delivery: 6000, total: 51000 });
  });

  it("multiplies each line's price by its quantity", () => {
    expect(cartTotals([{ ...coasters, qty: 3 }]).subtotal, "three coaster sets cost 3 × 15000 = 45000 paise").toBe(45000);
  });

  it("still charges delivery at exactly ₹999", () => {
    expect(cartTotals([{ ...tote, qty: 1 }]), "₹999 is not above ₹999, so delivery is still ₹60: 89900 + 6000").toEqual({ subtotal: 89900, discount: 0, delivery: 6000, total: 95900 });
  });

  it("delivers free above ₹999", () => {
    expect(cartTotals([{ ...tote, qty: 1 }, { ...keychain, qty: 1 }]), "a tote and a keychain are 101900 paise, above ₹999, so delivery is free").toEqual({ subtotal: 101900, discount: 0, delivery: 0, total: 101900 });
  });

  it("gives no Diwali offer at exactly ₹1,500", () => {
    expect(cartTotals([{ ...coasters, qty: 10 }]), "ten coaster sets are exactly ₹1,500, which is not above ₹1,500: no offer, free delivery").toEqual({ subtotal: 150000, discount: 0, delivery: 0, total: 150000 });
  });

  it("takes 10% off above ₹1,500", () => {
    expect(cartTotals([{ ...tote, qty: 2 }]), "two totes are 179800 paise: 17980 off, free delivery, 161820 to pay").toEqual({ subtotal: 179800, discount: 17980, delivery: 0, total: 161820 });
  });

  it("rounds the offer down to whole paise", () => {
    const odd = { id: "X-1", name: "A made-up item", paise: 150005, qty: 1 };
    expect(cartTotals([odd]).discount, "10% of 150005 is 15000.5 paise, rounded down to 15000").toBe(15000);
  });
});
