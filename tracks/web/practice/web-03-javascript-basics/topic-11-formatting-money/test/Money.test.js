import { describe, it, expect } from "vitest";
import { plainRupees, formatINR, priceTag, receiptRow } from "../src/money.js";

describe("Showing rupees", () => {
  it("shows paise as rupees with two decimals", () => {
    expect(plainRupees(15050), '15050 paise should show as "150.50"').toBe("150.50");
    expect(plainRupees(89900), '89900 paise should show as "899.00"').toBe("899.00");
    expect(plainRupees(129900), '129900 paise should show as "1299.00"').toBe("1299.00");
    expect(plainRupees(5), '5 paise should show as "0.05"').toBe("0.05");
  });

  it("formats paise as Indian rupees with the ₹ sign and commas", () => {
    expect(formatINR(129900), '129900 paise should show as "₹1,299.00"').toBe("₹1,299.00");
    expect(formatINR(15000), '15000 paise should show as "₹150.00"').toBe("₹150.00");
    expect(formatINR(15050), '15050 paise should show as "₹150.50"').toBe("₹150.50");
    expect(formatINR(5), '5 paise should show as "₹0.05"').toBe("₹0.05");
    expect(formatINR(0), '0 paise should show as "₹0.00"').toBe("₹0.00");
  });

  it("groups lakhs the Indian way", () => {
    expect(formatINR(12345678), '12345678 paise should show as "₹1,23,456.78", with lakh grouping').toBe("₹1,23,456.78");
  });

  it("writes a price tag", () => {
    expect(priceTag("Tote bag", 89900), 'the tag should read "Tote bag: ₹899.00"').toBe("Tote bag: ₹899.00");
    expect(priceTag("Coaster set (4)", 15000), 'the tag should read "Coaster set (4): ₹150.00"').toBe(
      "Coaster set (4): ₹150.00",
    );
  });

  it("lines up a receipt row: label to 12 characters, price to 10", () => {
    const keychain = receiptRow("Keychain", 12000);
    expect(keychain.length, "every receipt row should be 12 + 10 = 22 characters long").toBe(22);
    expect(keychain, 'the Keychain row should be "Keychain", 7 spaces, then "₹120.00"').toBe("Keychain" + " ".repeat(7) + "₹120.00");
    expect(receiptRow("Delivery", 6000), 'the Delivery row should be "Delivery", 8 spaces, then "₹60.00"').toBe(
      "Delivery" + " ".repeat(8) + "₹60.00",
    );
    expect(receiptRow("Total", 129900), 'the Total row should be "Total", 8 spaces, then "₹1,299.00"').toBe(
      "Total" + " ".repeat(8) + "₹1,299.00",
    );
  });
});
