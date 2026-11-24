import { describe, it, expect } from "vitest";
import { isPunePin } from "../src/pin.js";

describe("isPunePin", () => {
  it("accepts six digits starting with 411", () => {
    expect(isPunePin("411038"), "411038 (Kothrud) is in Pune").toBe(true);
    expect(isPunePin("411001"), "411001 is in Pune").toBe(true);
  });

  it("accepts a space after the third digit and spaces around it", () => {
    expect(isPunePin("411 038"), "\"411 038\" is a Pune PIN typed with a space").toBe(true);
    expect(isPunePin(" 411038 "), "spaces around the PIN should be ignored").toBe(true);
  });

  it("rejects PINs outside Pune", () => {
    expect(isPunePin("560001"), "560001 is in Bengaluru").toBe(false);
    expect(isPunePin("400001"), "400001 is in Mumbai").toBe(false);
  });

  it("rejects the wrong number of digits and letters", () => {
    expect(isPunePin("41103"), "five digits is not a PIN").toBe(false);
    expect(isPunePin("4110381"), "seven digits is not a PIN").toBe(false);
    expect(isPunePin("411O38"), "the letter O is not a zero").toBe(false);
    expect(isPunePin(""), "an empty box is not a PIN").toBe(false);
  });

  it("rejects a PIN stored as a number", () => {
    expect(isPunePin(411038), "a PIN is text; the number 411038 should give false").toBe(false);
  });
});
