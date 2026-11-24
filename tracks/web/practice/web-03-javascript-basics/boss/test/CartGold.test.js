import { describe, it, expect } from "vitest";
import { formatRupees, totalLine } from "../src/cart.js";

describe("INC-03 gold: the receipt shows rupees properly", () => {
  it("formats paise as Indian rupees", () => {
    expect(formatRupees(206820), '206820 paise should show as "₹2,068.20"').toBe("₹2,068.20");
    expect(formatRupees(30000), '30000 paise should show as "₹300.00"').toBe("₹300.00");
    expect(formatRupees(89900), '89900 paise should show as "₹899.00"').toBe("₹899.00");
    expect(formatRupees(5), '5 paise should show as "₹0.05"').toBe("₹0.05");
  });

  it("prints the total line for Mrs. Kapoor's order", () => {
    const order = [
      { code: "BAG", qty: 2 },
      { code: "BTY", qty: 1 },
      { code: "CST", qty: 1 },
    ];
    expect(totalLine(order), 'the receipt should say "Total: ₹2,068.20"').toBe("Total: ₹2,068.20");
  });

  it("prints the total line for a small order", () => {
    expect(totalLine([{ code: "KEY", qty: 1 }]), 'one keychain should say "Total: ₹120.00"').toBe("Total: ₹120.00");
  });
});
