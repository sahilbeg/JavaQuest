import { describe, it, expect } from "vitest";
import { extrasFor, lineTotal, cityOf, pinOf, qtyOrOne } from "../src/orders.ts";

describe("Orders with optional details", () => {
  it("charges extras per item only when they were asked for", () => {
    expect(extrasFor({ code: "BTY", qty: 2, giftWrap: true }), "two wrapped booties need two wraps: 6000 paise").toBe(6000);
    expect(extrasFor({ code: "KEY", qty: 1, name: "Riya" }), "one keychain with a name costs 5000 paise extra").toBe(5000);
    expect(extrasFor({ code: "AMI", qty: 2, giftWrap: true, name: "Golu" }), "two toys, each wrapped and named: (3000 + 5000) x 2 = 16000").toBe(16000);
    expect(extrasFor({ code: "CST", qty: 3 }), "a line with no giftWrap and no name has no extras").toBe(0);
    expect(extrasFor({ code: "KEY", qty: 1, name: "" }), "an empty name means nothing to stitch, so 0").toBe(0);
  });

  it("totals a line with its price and its extras", () => {
    expect(lineTotal({ code: "BTY", qty: 2, giftWrap: true }), "two booties are 70000, plus 6000 for two wraps: 76000").toBe(76000);
    expect(lineTotal({ code: "BAG", qty: 1 }), "one tote bag with no extras is 89900").toBe(89900);
  });

  it("finds the customer's city, or sends them to the stall", () => {
    expect(cityOf({ name: "Asha", address: { area: "Aundh", city: "Pune" } }), "Asha lives in Pune").toBe("Pune");
    expect(cityOf({ name: "Ravi", phone: "98765 43210" }), "Ravi has no address, so it's \"Pickup at the stall\"").toBe("Pickup at the stall");
  });

  it("finds the PIN code, or says it's missing", () => {
    expect(pinOf({ name: "Asha", address: { area: "Aundh", city: "Pune", pin: "411007" } }), "Asha's PIN is 411007").toBe("411007");
    expect(pinOf({ name: "Meena", address: { area: "Baner", city: "Pune" } }), "an address without a PIN gives \"PIN missing\"").toBe("PIN missing");
    expect(pinOf({ name: "Ravi" }), "no address at all also gives \"PIN missing\"").toBe("PIN missing");
  });

  it("uses 1 for a missing quantity, but keeps 0", () => {
    expect(qtyOrOne(3), "a quantity of 3 stays 3").toBe(3);
    expect(qtyOrOne(), "a quantity that was left out is 1").toBe(1);
    expect(qtyOrOne(0), "0 is a cancelled line and must stay 0, not become 1").toBe(0);
  });
});
