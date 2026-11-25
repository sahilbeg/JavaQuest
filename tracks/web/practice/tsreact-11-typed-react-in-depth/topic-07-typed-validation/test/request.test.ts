import { describe, it, expect } from "vitest";
import { validateRequest, toRequest } from "../src/request.ts";

const GOOD = { name: "Asha Rao", phone: "98765 43210", colour: "Mint", qty: "2", stitchedName: "Golu" };

describe("validateRequest", () => {
  it("finds nothing wrong with a good request", () => {
    expect(validateRequest(GOOD), "a complete, sensible request should give {}").toEqual({});
  });

  it("asks for a name", () => {
    expect(validateRequest({ ...GOOD, name: "   " }).name, "a name of only spaces should give Please enter your name.").toBe(
      "Please enter your name.",
    );
  });

  it("wants exactly 10 digits in the phone", () => {
    const message = "Phone needs 10 digits, like 98765 43210.";
    expect(validateRequest({ ...GOOD, phone: "9876" }).phone, "9876 is too short").toBe(message);
    expect(validateRequest({ ...GOOD, phone: "98765 432100" }).phone, "11 digits is too many").toBe(message);
    expect(validateRequest({ ...GOOD, phone: "98765 4321x" }).phone, "a letter is not a digit").toBe(message);
    expect(validateRequest({ ...GOOD, phone: "9876543210" }).phone, "10 digits with no spaces is fine").toBeUndefined();
  });

  it("wants a whole number from 1 to 20", () => {
    const message = "Order between 1 and 20.";
    expect(validateRequest({ ...GOOD, qty: "0" }).qty, "0 is below 1").toBe(message);
    expect(validateRequest({ ...GOOD, qty: "21" }).qty, "21 is above 20").toBe(message);
    expect(validateRequest({ ...GOOD, qty: "1.5" }).qty, "1.5 isn't a whole number").toBe(message);
    expect(validateRequest({ ...GOOD, qty: "" }).qty, "an empty box isn't a number").toBe(message);
    expect(validateRequest({ ...GOOD, qty: " 20 " }).qty, "20 with spaces around it is fine").toBeUndefined();
  });

  it("keeps the stitched name short", () => {
    expect(validateRequest({ ...GOOD, stitchedName: "Happy birthday" }).stitchedName, "14 letters is too long to stitch on").toBe(
      "Names can be up to 12 letters.",
    );
    expect(validateRequest({ ...GOOD, stitchedName: "" }).stitchedName, "the stitched name is optional").toBeUndefined();
  });

  it("gives a message for every bad field at once", () => {
    const errors = validateRequest({ name: "", phone: "1", colour: "Indigo", qty: "50", stitchedName: "" });
    expect(Object.keys(errors).sort(), "name, phone and qty are all wrong").toEqual(["name", "phone", "qty"]);
  });
});

describe("toRequest", () => {
  it("cleans the form into the request Meera Aunty needs", () => {
    expect(toRequest({ name: "  Asha Rao ", phone: "98765 43210", colour: "Mint", qty: " 2", stitchedName: " Golu " }), "the clean request").toEqual({
      name: "Asha Rao",
      phone: "9876543210",
      colour: "Mint",
      qty: 2,
      stitchedName: "Golu",
    });
  });

  it("uses null for an empty stitched name", () => {
    expect(toRequest({ ...GOOD, stitchedName: "   " }).stitchedName, "no stitched name should be null").toBeNull();
  });
});
