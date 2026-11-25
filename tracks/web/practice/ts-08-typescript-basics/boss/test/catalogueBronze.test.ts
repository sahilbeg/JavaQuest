import { describe, it, expect } from "vitest";
import { toPaise, lineTotal, cartTotal } from "../src/catalogue.ts";

describe("INC-08 bronze: prices that arrive as text become numbers", () => {
  it("turns a price into a number of paise, whether it came as a number or as text", () => {
    expect(toPaise(15000), "a price that is already the number 15000 stays 15000").toBe(15000);
    expect(toPaise("12000"), "the text \"12000\" from the sheet should become the number 12000").toBe(12000);
    expect(toPaise(" 35000 "), "the text \" 35000 \" should lose its spaces and become 35000").toBe(35000);
    expect(typeof toPaise("89900"), "toPaise must give back a real number, not text that looks like one").toBe("number");
  });

  it("prices a keychain line correctly, with and without gift wrap", () => {
    expect(lineTotal({ code: "KEY", qty: 1 }), "one keychain is 12000 paise, not 120000").toBe(12000);
    expect(lineTotal({ code: "KEY", qty: 2, giftWrap: true }), "two wrapped keychains are (12000 + 3000) x 2 = 30000 paise").toBe(30000);
    expect(lineTotal({ code: "BTY", qty: 1 }), "baby booties are 35000 paise, not NaN").toBe(35000);
  });

  it("totals Mrs. Deshpande's order to ₹270, not ₹1,350", () => {
    expect(
      cartTotal([
        { code: "KEY", qty: 1 },
        { code: "CST", qty: 1 },
      ]),
      "a keychain and a coaster set are 12000 + 15000 = 27000 paise",
    ).toBe(27000);
  });

  it("totals one of everything on the price sheet", () => {
    const oneOfEach = ["CST", "KEY", "AMI", "BAG", "BTY"].map((code) => ({ code, qty: 1 }));
    expect(cartTotal(oneOfEach), "15000 + 12000 + 45000 + 89900 + 35000 = 196900 paise").toBe(196900);
  });
});
