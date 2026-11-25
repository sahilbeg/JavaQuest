import { describe, it, expect } from "vitest";
import { cartTotals } from "../src/pricing.js";

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
const coasters = { id: "DC-1110", name: "Indigo coaster set", paise: 15000 };
const keychain = { id: "DC-1210", name: "Rani pink keychain", paise: 12000 };
const booties = { id: "DC-1410", name: "Off-white baby booties", paise: 35000 };

describe("Stage 1: cartTotals", () => {
  it("gives all zeros for an empty cart, with no delivery charge", () => {
    expect(cartTotals([]), "an empty cart costs nothing, not even delivery").toEqual({ subtotal: 0, discount: 0, delivery: 0, total: 0 });
  });

  it("adds ₹60 delivery to a small order", () => {
    expect(cartTotals([{ ...keychain, qty: 1 }]), "one keychain: 12000 + 6000 delivery = 18000").toEqual({ subtotal: 12000, discount: 0, delivery: 6000, total: 18000 });
  });

  it("multiplies each line's price by its quantity", () => {
    expect(cartTotals([{ ...booties, qty: 2 }, { ...keychain, qty: 1 }]).subtotal, "two booties and a keychain: 2 × 35000 + 12000 = 82000").toBe(82000);
  });

  it("still charges delivery at exactly ₹999", () => {
    const exactly999 = { id: "X-999", name: "A test item at ₹999", paise: 99900, qty: 1 };
    expect(cartTotals([exactly999]), "₹999 is not above ₹999, so delivery is still ₹60: 99900 + 6000").toEqual({ subtotal: 99900, discount: 0, delivery: 6000, total: 105900 });
  });

  it("delivers free above ₹999", () => {
    expect(cartTotals([{ ...golu, qty: 2 }, { ...keychain, qty: 1 }]), "two Golus and a keychain are 102000 paise, above ₹999, so delivery is free").toEqual({ subtotal: 102000, discount: 0, delivery: 0, total: 102000 });
  });

  it("gives no Diwali offer at exactly ₹1,500", () => {
    expect(cartTotals([{ ...golu, qty: 3 }, { ...coasters, qty: 1 }]), "three Golus and a coaster set are exactly 150000 paise: no offer, free delivery").toEqual({ subtotal: 150000, discount: 0, delivery: 0, total: 150000 });
  });

  it("takes 10% off above ₹1,500", () => {
    expect(cartTotals([{ ...golu, qty: 4 }]), "four Golus are 180000 paise: 18000 off, free delivery, 162000 to pay").toEqual({ subtotal: 180000, discount: 18000, delivery: 0, total: 162000 });
  });

  it("rounds the offer down to whole paise", () => {
    const odd = { id: "X-1", name: "A made-up item", paise: 150019, qty: 1 };
    expect(cartTotals([odd]).discount, "10% of 150019 is 15001.9 paise, rounded down to 15001").toBe(15001);
  });
});
