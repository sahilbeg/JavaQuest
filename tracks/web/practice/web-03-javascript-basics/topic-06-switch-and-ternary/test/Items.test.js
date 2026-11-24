import { describe, it, expect } from "vitest";
import { priceOf, itemName, isWeekend, itemsText } from "../src/items.js";

describe("Item codes", () => {
  it("gives the price in paise for every code on the price list", () => {
    expect(priceOf("CST"), "a coaster set costs 15000 paise").toBe(15000);
    expect(priceOf("KEY"), "a keychain costs 12000 paise").toBe(12000);
    expect(priceOf("AMI"), "an amigurumi costs 45000 paise").toBe(45000);
    expect(priceOf("BAG"), "a tote bag costs 89900 paise").toBe(89900);
    expect(priceOf("BTY"), "baby booties cost 35000 paise").toBe(35000);
  });

  it("gives null for a code that is not on the price list", () => {
    expect(priceOf("XYZ"), "XYZ is not a Dhaaga item, so its price is null").toBe(null);
    expect(priceOf("ami"), "codes are capitals, so \"ami\" does not match AMI").toBe(null);
  });

  it("gives the full name for every code", () => {
    expect(itemName("CST"), 'CST is "Coaster set (4)"').toBe("Coaster set (4)");
    expect(itemName("KEY"), 'KEY is "Keychain"').toBe("Keychain");
    expect(itemName("AMI"), 'AMI is "Amigurumi"').toBe("Amigurumi");
    expect(itemName("BAG"), 'BAG is "Tote bag"').toBe("Tote bag");
    expect(itemName("BTY"), 'BTY is "Baby booties"').toBe("Baby booties");
    expect(itemName("XYZ"), 'an unknown code gives "Unknown item"').toBe("Unknown item");
  });

  it("knows which days are the weekend", () => {
    expect(isWeekend("Sat"), "Saturday is the weekend").toBe(true);
    expect(isWeekend("Sun"), "Sunday is the weekend").toBe(true);
    expect(isWeekend("Mon"), "Monday is not the weekend").toBe(false);
    expect(isWeekend("Fri"), "Friday is not the weekend").toBe(false);
  });

  it("says item or items to match the count", () => {
    expect(itemsText(1), 'one thing is "1 item"').toBe("1 item");
    expect(itemsText(3), 'three things are "3 items"').toBe("3 items");
    expect(itemsText(0), 'nothing is "0 items"').toBe("0 items");
  });
});
