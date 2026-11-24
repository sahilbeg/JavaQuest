import { describe, it, expect } from "vitest";
import { validateAddress } from "../src/validateAddress.js";

const GOOD = { name: "Asha Rao", phone: "98765 43210", line1: "12 Mint Lane, Aundh", pincode: "411007" };

describe("validateAddress", () => {
  it("finds no problems with a good address", () => {
    expect(validateAddress(GOOD), "a correct address should give an empty object").toEqual({});
  });

  it("finds all four problems in an empty form", () => {
    const errors = validateAddress({ name: "", phone: "", line1: "", pincode: "" });
    expect(Object.keys(errors).sort(), "an empty form should have errors for line1, name, phone and pincode").toEqual([
      "line1",
      "name",
      "phone",
      "pincode",
    ]);
  });

  it("needs a name that isn't just spaces", () => {
    expect(validateAddress({ ...GOOD, name: "   " }).name, "a name of only spaces is empty").toBe("Please enter your name.");
  });

  it("needs a phone with exactly 10 digits, allowing spaces", () => {
    const message = "Phone needs 10 digits, like 98765 43210.";
    expect(validateAddress({ ...GOOD, phone: "98765 4321" }).phone, "9 digits is too short").toBe(message);
    expect(validateAddress({ ...GOOD, phone: "98765-43210" }).phone, "a dash is not a digit").toBe(message);
    expect(validateAddress({ ...GOOD, phone: "9876543210" }).phone, "10 digits with no space is fine").toBeUndefined();
  });

  it("needs the house and street", () => {
    expect(validateAddress({ ...GOOD, line1: "" }).line1, "an empty address line needs a message").toBe(
      "Please enter your house and street.",
    );
  });

  it("needs a 6-digit PIN code that doesn't start with 0", () => {
    const message = "PIN code needs 6 digits.";
    expect(validateAddress({ ...GOOD, pincode: "41100" }).pincode, "5 digits is too short").toBe(message);
    expect(validateAddress({ ...GOOD, pincode: "011007" }).pincode, "Indian PIN codes never start with 0").toBe(message);
    expect(validateAddress({ ...GOOD, pincode: " 411007 " }).pincode, "spaces around a good PIN code are fine").toBeUndefined();
  });
});
