import { describe, it, expect } from "vitest";
import { formatRupees } from "../src/money.ts";
import { priceOf } from "../src/prices.ts";
import { quoteLine, quoteTotal } from "../src/quote.ts";
import { TEAM_OPTIONS } from "../src/config.ts";

describe("The price calculator, split into typed modules", () => {
  it("money.ts shows paise as rupees the Indian way", () => {
    expect(formatRupees(129900), "129900 paise should show as ₹1,299.00").toBe("₹1,299.00");
    expect(formatRupees(15000), "15000 paise should show as ₹150.00").toBe("₹150.00");
    expect(formatRupees(10000000), "₹1 lakh should use Indian grouping: ₹1,00,000.00").toBe("₹1,00,000.00");
  });

  it("prices.ts looks up the price of a code", () => {
    expect(priceOf("BAG"), "a tote bag costs 89900 paise").toBe(89900);
    expect(priceOf("CST"), "a coaster set costs 15000 paise").toBe(15000);
  });

  it("quote.ts writes one quote line from the other modules", () => {
    expect(quoteLine({ code: "BAG", qty: 2 }), "two tote bags should read \"BAG x 2: ₹1,798.00\"").toBe("BAG x 2: ₹1,798.00");
    expect(quoteLine({ code: "KEY", qty: 1 }), "one keychain should read \"KEY x 1: ₹120.00\"").toBe("KEY x 1: ₹120.00");
  });

  it("quote.ts totals every line in paise", () => {
    expect(quoteTotal([{ code: "BAG", qty: 2 }, { code: "BTY", qty: 1 }]), "two tote bags and booties are 179800 + 35000 = 214800 paise").toBe(214800);
    expect(quoteTotal([]), "an empty quote is 0 paise").toBe(0);
  });

  it("config.ts holds the team's compiler settings", () => {
    expect(TEAM_OPTIONS.strict, "strict must be on, so every check is switched on").toBe(true);
    expect(TEAM_OPTIONS.noEmit, "noEmit must be on: Vite builds the files, and tsc only checks").toBe(true);
    expect(TEAM_OPTIONS.target, "target should be \"es2022\"").toBe("es2022");
    expect(TEAM_OPTIONS.module, "module should be \"esnext\", for import and export").toBe("esnext");
    expect(TEAM_OPTIONS.moduleResolution, "moduleResolution should be \"bundler\", because Vite finds the files").toBe("bundler");
  });
});
