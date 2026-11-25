import { describe, it, expect } from "vitest";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { cheapestLine, describeError, isItemCode, priceOf } from "../src/lookup.ts";

describe("isItemCode", () => {
  it("says yes to the five real codes", () => {
    for (const code of ["CST", "KEY", "AMI", "BAG", "BTY"]) {
      expect(isItemCode(code), `${code} is a real item code (TODO 1)`).toBe(true);
    }
  });

  it("says no to codes that don't exist, even keys every object has", () => {
    for (const text of ["XYZ", "ami", "", "toString", "constructor", "hasOwnProperty", "__proto__"]) {
      expect(isItemCode(text), `"${text}" is not an item code. "in" finds keys like toString on every object: check PRICES' own keys (TODO 1)`).toBe(false);
    }
  });
});

describe("priceOf", () => {
  it("finds the price of a code, typed with spaces or small letters", () => {
    expect(priceOf("AMI"), "AMI, the amigurumi, costs 45000 paise (TODO 2)").toBe(45000);
    expect(priceOf(" bag "), "\" bag \" should be read as BAG, 89900 paise: trim it and make it upper case (TODO 2)").toBe(89900);
    expect(priceOf("Key"), "\"Key\" should be read as KEY, 12000 paise").toBe(12000);
  });

  it("gives null, never undefined or NaN, for a code that doesn't exist", () => {
    expect(priceOf("XYZ"), "there is no item XYZ, so the price is null").toBe(null);
    expect(priceOf(""), "an empty box has no price: null").toBe(null);
    expect(priceOf("   "), "a box of spaces has no price: null").toBe(null);
  });
});

describe("cheapestLine", () => {
  it("gives null for an empty cart", () => {
    expect(cheapestLine([]), "an empty cart has no cheapest line, so the answer is null").toBe(null);
  });

  it("finds the line whose item costs least", () => {
    const lines = [
      { code: "BAG" as const, qty: 1 },
      { code: "KEY" as const, qty: 2 },
      { code: "AMI" as const, qty: 1 },
    ];
    expect(cheapestLine(lines), "the keychain (12000 paise) is cheaper than the tote bag and the amigurumi (TODO 3)").toEqual({ code: "KEY", qty: 2 });
  });

  it("keeps the first line when two cost the same, and leaves the cart in its order", () => {
    const lines = [
      { code: "AMI" as const, qty: 1 },
      { code: "CST" as const, qty: 1 },
      { code: "CST" as const, qty: 3 },
    ];
    expect(cheapestLine(lines), "two coaster lines cost the same per item: the first one (qty 1) wins (TODO 3)").toEqual({ code: "CST", qty: 1 });
    expect(lines.map((line) => line.code), "cheapestLine must not sort or change the lines it was given").toEqual(["AMI", "CST", "CST"]);
  });
});

describe("describeError", () => {
  it("uses the message of an Error", () => {
    expect(describeError(new Error("The UPI app timed out.")), "an Error should give its message (TODO 4)").toBe("The UPI app timed out.");
  });

  it("uses a thrown string as it is", () => {
    expect(describeError("Card declined."), "a thrown string should come back as it is (TODO 4)").toBe("Card declined.");
  });

  it("has a kind fallback for anything else", () => {
    for (const thrown of [404, null, undefined, { code: 500 }]) {
      expect(describeError(thrown), `throwing ${JSON.stringify(thrown)} should give "Something went wrong." (TODO 4)`).toBe("Something went wrong.");
    }
  });
});

describe("The code itself", () => {
  it("never switches the type checks off with any, as or !", () => {
    const code = readFileSync(join(process.cwd(), "src", "lookup.ts"), "utf8")
      .replace(/\/\*[\s\S]*?\*\//g, "")
      .replace(/\/\/.*$/gm, "");
    expect(/\bany\b/.test(code), "lookup.ts should not use the any type").toBe(false);
    expect(/\bas\s+[A-Za-z{[(]/.test(code), "lookup.ts should not use as to tell the compiler what a value is: check it instead").toBe(false);
    expect(/[\w)\]]!(?=[.;,)\][\s])/.test(code), "lookup.ts should not use ! to say a value can't be null: handle the null case").toBe(false);
  });
});
