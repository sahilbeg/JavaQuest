import { describe, it, expect } from "vitest";
import { modeLabel, orderLabel, totalFor, dayTotals, readMode } from "../src/payments.ts";

const saturday = [
  { orderId: "order-17", mode: "UPI", paise: 89900 },
  { orderId: "order-18", mode: "CASH", paise: 15000 },
  { orderId: "order-19", mode: "UPI", paise: 12000 },
  { orderId: "order-20", mode: "CASH", paise: 35000 },
];

describe("Payments with union and literal types", () => {
  it("labels each payment mode for the receipt", () => {
    expect(modeLabel("CASH"), "CASH should read \"Cash\"").toBe("Cash");
    expect(modeLabel("UPI"), "UPI should read \"UPI\"").toBe("UPI");
    expect(modeLabel("CARD"), "CARD should read \"Card\"").toBe("Card");
  });

  it("labels an order id given as a number or as text", () => {
    expect(orderLabel(17), "the number 17 should become \"order-17\"").toBe("order-17");
    expect(orderLabel("17"), "the text \"17\" should become \"order-17\"").toBe("order-17");
    expect(orderLabel("order-17"), "\"order-17\" already has its prefix and should stay as it is").toBe("order-17");
  });

  it("adds up the payments made with one mode", () => {
    expect(totalFor(saturday, "UPI"), "the UPI payments are 89900 + 12000 = 101900 paise").toBe(101900);
    expect(totalFor(saturday, "CASH"), "the cash payments are 15000 + 35000 = 50000 paise").toBe(50000);
    expect(totalFor(saturday, "CARD"), "nobody paid by card, so 0 paise").toBe(0);
  });

  it("totals the whole day by mode", () => {
    expect(dayTotals(saturday), "Saturday: 50000 paise cash, 101900 by UPI and 0 by card").toEqual({ CASH: 50000, UPI: 101900, CARD: 0 });
    expect(dayTotals([]), "a day with no payments has 0 for every mode").toEqual({ CASH: 0, UPI: 0, CARD: 0 });
  });

  it("reads a payment mode that Raju typed by hand", () => {
    expect(readMode("UPI"), "\"UPI\" is a known mode").toBe("UPI");
    expect(readMode(" upi "), "\" upi \" should be trimmed and upper-cased to \"UPI\"").toBe("UPI");
    expect(readMode("Cash"), "\"Cash\" should become \"CASH\"").toBe("CASH");
    expect(readMode("card"), "\"card\" should become \"CARD\"").toBe("CARD");
    expect(readMode("GPay"), "\"GPay\" isn't one of the three modes, so it is \"OTHER\"").toBe("OTHER");
    expect(readMode(""), "an empty box is \"OTHER\"").toBe("OTHER");
  });
});
