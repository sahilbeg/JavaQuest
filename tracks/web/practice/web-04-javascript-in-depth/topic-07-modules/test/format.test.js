import { describe, it, expect } from "vitest";
import rupees from "../src/format.js";
import * as formatModule from "../src/format.js";

describe("format.js", () => {
  it("shows paise as rupees with two decimals", () => {
    expect(rupees(15000), "15000 paise should show as ₹150.00").toBe("₹150.00");
    expect(rupees(6000), "6000 paise should show as ₹60.00").toBe("₹60.00");
  });

  it("puts a comma in amounts of ₹1,000 and more", () => {
    expect(rupees(179800), "179800 paise should show as ₹1,798.00").toBe("₹1,798.00");
  });

  it("keeps formatter private", () => {
    expect(formatModule.formatter, "formatter is a helper for format.js only, so it must not be exported").toBeUndefined();
  });

  it("exports rupees as its default", () => {
    expect(formatModule.default, "the default export of format.js should be the rupees function").toBe(rupees);
  });
});
