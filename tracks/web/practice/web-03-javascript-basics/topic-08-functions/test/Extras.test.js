import { describe, it, expect } from "vitest";
import { lineTotal, extrasFor, itemCost, deliveryFor } from "../src/extras.js";

describe("Extras functions", () => {
  it("multiplies the price by the quantity", () => {
    expect(lineTotal(15000, 2), "two coaster sets cost 30000 paise").toBe(30000);
    expect(lineTotal(89900, 3), "three tote bags cost 269700 paise").toBe(269700);
  });

  it("uses a quantity of 1 when qty is left out", () => {
    expect(lineTotal(45000), "lineTotal(45000) should treat the missing qty as 1 and give 45000").toBe(45000);
  });

  it("adds up the extras for one item", () => {
    expect(extrasFor(false, ""), "no gift wrap and no name cost nothing").toBe(0);
    expect(extrasFor(true, ""), "gift wrap alone costs 3000 paise").toBe(3000);
    expect(extrasFor(false, "Golu"), "a stitched name alone costs 5000 paise").toBe(5000);
    expect(extrasFor(true, "Riya"), "gift wrap and a name cost 3000 + 5000 = 8000 paise").toBe(8000);
    expect(extrasFor(true), "when the name is left out, only the gift wrap counts").toBe(3000);
  });

  it("works out an item's full cost, with extras for every item", () => {
    expect(
      itemCost(45000, 2, true, "Golu"),
      "two amigurumi at 45000, each wrapped and named, cost 90000 + 2 x 8000 = 106000 paise",
    ).toBe(106000);
    expect(itemCost(35000, 1, false, ""), "one pair of booties with no extras costs 35000 paise").toBe(35000);
    expect(itemCost(12000, 3, true, ""), "three wrapped keychains cost 36000 + 3 x 3000 = 45000 paise").toBe(45000);
  });

  it("charges delivery only up to ₹999", () => {
    expect(deliveryFor(50000), "a ₹500 order pays 6000 paise for delivery").toBe(6000);
    expect(deliveryFor(99900), "exactly ₹999 still pays 6000 paise").toBe(6000);
    expect(deliveryFor(99901), "above ₹999, delivery is free").toBe(0);
  });
});
