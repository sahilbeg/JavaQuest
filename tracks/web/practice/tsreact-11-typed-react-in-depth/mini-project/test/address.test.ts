import { describe, it, expect } from "vitest";
import { validateAddress } from "../src/address.ts";

const GOOD = { name: "Asha Rao", phone: "98765 43210", pincode: "411007" };

describe("validateAddress", () => {
  it("finds nothing wrong with a good address", () => {
    expect(validateAddress(GOOD), "a complete address should give {}").toEqual({});
  });

  it("asks for a name", () => {
    expect(validateAddress({ ...GOOD, name: "  " }).name, "a blank name should give Please enter your name.").toBe("Please enter your name.");
  });

  it("wants 10 digits in the phone", () => {
    expect(validateAddress({ ...GOOD, phone: "98765" }).phone, "98765 is too short").toBe("Phone needs 10 digits, like 98765 43210.");
    expect(validateAddress({ ...GOOD, phone: "9876543210" }).phone, "10 digits without spaces is fine").toBeUndefined();
  });

  it("wants 6 digits in the PIN code", () => {
    expect(validateAddress({ ...GOOD, pincode: "4110" }).pincode, "4110 is too short").toBe("PIN code needs 6 digits.");
    expect(validateAddress({ ...GOOD, pincode: "41100A" }).pincode, "a letter isn't a digit").toBe("PIN code needs 6 digits.");
  });
});
