import { describe, it, expect } from "vitest";
import { receiptLine, changePaise, totalsByMode } from "../src/payment.ts";
import type { Payment } from "../src/payment.ts";

const cash: Payment = { mode: "CASH", paise: 45000, tenderedPaise: 50000 };
const upi: Payment = { mode: "UPI", paise: 45000, upiRef: "UPI-5521" };
const card: Payment = { mode: "CARD", paise: 89900, last4: "4242" };

describe("receiptLine", () => {
  it("shows the change for a cash payment", () => {
    expect(receiptLine(cash), "₹500 handed over for ₹450 means ₹50.00 change").toBe("Cash ₹450.00, change ₹50.00");
  });

  it("shows the reference for a UPI payment, and no change", () => {
    expect(receiptLine(upi), "a UPI receipt shows its reference, never a change amount").toBe("UPI ₹450.00, ref UPI-5521");
  });

  it("shows the last four digits for a card payment", () => {
    expect(receiptLine(card), "a card receipt ends with the card's last four digits").toBe("Card ₹899.00, ending 4242");
  });

  it("throws for a mode the code doesn't know", () => {
    const cheque = { mode: "CHEQUE", paise: 45000 } as unknown as Payment;
    expect(() => receiptLine(cheque), "an unknown mode should throw through assertNever, not print a wrong receipt").toThrow("Unknown payment");
  });
});

describe("changePaise", () => {
  it("gives the change for cash", () => {
    expect(changePaise(cash), "50000 handed over for 45000 is 5000 paise of change").toBe(5000);
  });

  it("gives 0 for UPI and card", () => {
    expect(changePaise(upi), "UPI payments have no change").toBe(0);
    expect(changePaise(card), "card payments have no change").toBe(0);
  });
});

describe("totalsByMode", () => {
  it("adds up each mode's payments", () => {
    const day: Payment[] = [cash, upi, card, { mode: "UPI", paise: 12000, upiRef: "UPI-5530" }];
    expect(totalsByMode(day), "cash 45000, UPI 45000 + 12000, card 89900").toEqual({ CASH: 45000, UPI: 57000, CARD: 89900 });
  });

  it("lists every mode with 0 on a quiet day", () => {
    expect(totalsByMode([]), "with no payments, every mode should be 0").toEqual({ CASH: 0, UPI: 0, CARD: 0 });
  });
});
