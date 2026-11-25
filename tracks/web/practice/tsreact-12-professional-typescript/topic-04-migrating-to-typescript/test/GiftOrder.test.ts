import { describe, it, expect } from "vitest";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { extrasPaise, lineTotal, orderSummary } from "../src/giftOrder.ts";

const NO_EXTRAS = { giftWrap: false, stitchedName: "" };
const GOLU_GIFT = {
  id: "DC-1042",
  name: "Golu the elephant",
  paise: 45000,
  qty: 2,
  gift: { giftWrap: true, stitchedName: "Aarav" },
};
const KEYCHAIN = { id: "DC-1201", name: "Mint keychain", paise: 12000, qty: 1, gift: NO_EXTRAS };

describe("extrasPaise", () => {
  it("charges nothing when there are no extras", () => {
    expect(extrasPaise(NO_EXTRAS), "no wrap and no name cost 0 paise").toBe(0);
  });

  it("charges 3000 paise for gift wrap", () => {
    expect(extrasPaise({ giftWrap: true, stitchedName: "" }), "gift wrap is ₹30, 3000 paise. The field is giftWrap, with a capital W (TODO 2)").toBe(3000);
  });

  it("charges 5000 paise for a stitched name, and 8000 for both", () => {
    expect(extrasPaise({ giftWrap: false, stitchedName: "Aarav" }), "a stitched name is ₹50, 5000 paise").toBe(5000);
    expect(extrasPaise({ giftWrap: true, stitchedName: "Aarav" }), "wrap and a name are 3000 + 5000 = 8000 paise (TODO 2)").toBe(8000);
  });

  it("doesn't charge for a name of only spaces", () => {
    expect(extrasPaise({ giftWrap: false, stitchedName: "   " }), "\"   \" is no name, so it costs nothing: trim it first (TODO 2)").toBe(0);
  });
});

describe("lineTotal", () => {
  it("is the price times the quantity for a line with no extras", () => {
    expect(lineTotal(KEYCHAIN), "one keychain is 12000 paise. The field is qty, not quantity (TODO 3)").toBe(12000);
  });

  it("charges the extras for every item in the line", () => {
    expect(lineTotal(GOLU_GIFT), "two wrapped Golus with names cost (45000 + 3000 + 5000) × 2 = 106000 paise (TODO 3)").toBe(106000);
  });
});

describe("orderSummary", () => {
  it("counts every item and adds up every line", () => {
    const summary = orderSummary({ id: "order-17", lines: [GOLU_GIFT, KEYCHAIN] });
    expect(summary, "2 Golus and 1 keychain are 3 items, and 106000 + 12000 = 118000 paise (TODO 4)").toBe("3 items, ₹1,180.00");
  });

  it("says item, not items, for one", () => {
    expect(orderSummary({ id: "order-18", lines: [KEYCHAIN] }), "one keychain should read \"1 item, ₹120.00\" (TODO 4)").toBe("1 item, ₹120.00");
  });
});

describe("The finished move", () => {
  it("has no any left in the code", () => {
    const code = readFileSync(join(process.cwd(), "src", "giftOrder.ts"), "utf8")
      .replace(/\/\*[\s\S]*?\*\//g, "")
      .replace(/\/\/.*$/gm, "");
    expect(/\bany\b/.test(code), "giftOrder.ts should have no any left: give GiftOptions, GiftLine and GiftOrder real object types (TODO 1)").toBe(false);
  });
});
