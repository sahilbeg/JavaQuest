import { describe, it, expect } from "vitest";
import { receiptLine, deliveryLine } from "../src/receipt.js";

describe("receiptLine", () => {
  it("shows the quantity, the code and the line's price", () => {
    expect(receiptLine("CST", 2), "two coaster sets should read 2 x CST = ₹300.00").toBe("2 x CST = ₹300.00");
    expect(receiptLine("AMI", 1), "one amigurumi should read 1 x AMI = ₹450.00").toBe("1 x AMI = ₹450.00");
  });

  it("formats big lines with a comma", () => {
    expect(receiptLine("BAG", 2), "two tote bags should read 2 x BAG = ₹1,798.00").toBe("2 x BAG = ₹1,798.00");
  });
});

describe("deliveryLine", () => {
  it("charges ₹60 for a small order", () => {
    expect(deliveryLine(45000), "a ₹450 order pays ₹60 delivery").toBe("Delivery: ₹60.00");
  });

  it("still charges at exactly ₹999", () => {
    expect(deliveryLine(99900), "delivery is free only above ₹999, so ₹999 still pays").toBe("Delivery: ₹60.00");
  });

  it("is free above ₹999", () => {
    expect(deliveryLine(105000), "a ₹1,050 order gets free delivery").toBe("Delivery: free");
  });
});
