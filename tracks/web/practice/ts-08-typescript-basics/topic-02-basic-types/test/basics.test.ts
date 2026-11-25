import { describe, it, expect } from "vitest";
import { toPaise, parseQty, rupeesText, canMakeToys, stockLabel } from "../src/basics.ts";

describe("Basic types in the price calculator", () => {
  it("turns rupees into the nearest whole paisa", () => {
    expect(toPaise(450), "₹450 is 45000 paise").toBe(45000);
    expect(toPaise(899), "₹899 is 89900 paise").toBe(89900);
    expect(toPaise(0.29), "₹0.29 is 29 paise, even though 0.29 * 100 is 28.999999999999996").toBe(29);
    expect(toPaise(2068.2), "₹2,068.20 is 206820 paise, not 206819").toBe(206820);
  });

  it("turns typed text into a quantity", () => {
    expect(parseQty("3"), "the text \"3\" is the number 3").toBe(3);
    expect(parseQty(" 2 "), "spaces around \" 2 \" should be ignored, giving 2").toBe(2);
    expect(parseQty("0"), "\"0\" is a quantity of 0").toBe(0);
  });

  it("gives 0 for text that isn't a whole quantity", () => {
    expect(parseQty("two"), "\"two\" isn't a number, so the quantity is 0").toBe(0);
    expect(parseQty("1.5"), "nobody can buy one and a half keychains, so \"1.5\" gives 0").toBe(0);
    expect(parseQty("-1"), "a quantity can't be negative, so \"-1\" gives 0").toBe(0);
    expect(parseQty(""), "an empty box gives 0").toBe(0);
  });

  it("shows paise as rupees with two decimals", () => {
    expect(rupeesText(45000), "45000 paise should show as ₹450.00").toBe("₹450.00");
    expect(rupeesText(2999), "2999 paise should show as ₹29.99").toBe("₹29.99");
    expect(rupeesText(5), "5 paise should show as ₹0.05").toBe("₹0.05");
  });

  it("says whether there is enough yarn for the toys", () => {
    expect(canMakeToys(130, 3), "three toys need 120 g, and 130 g is enough").toBe(true);
    expect(canMakeToys(40, 1), "one toy needs exactly 40 g, and 40 g is enough").toBe(true);
    expect(canMakeToys(130, 4), "four toys need 160 g, and 130 g is not enough").toBe(false);
  });

  it("writes a stock label for the shelf", () => {
    expect(stockLabel("Keychain", 3), "3 keychains left should read \"Keychain: 3 left\"").toBe("Keychain: 3 left");
    expect(stockLabel("Tote bag", 1), "1 tote bag left should read \"Tote bag: 1 left\"").toBe("Tote bag: 1 left");
    expect(stockLabel("Golu the elephant", 0), "none left should read \"Golu the elephant: sold out\"").toBe(
      "Golu the elephant: sold out",
    );
  });
});
