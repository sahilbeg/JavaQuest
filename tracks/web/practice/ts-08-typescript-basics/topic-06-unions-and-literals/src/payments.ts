// The day's payments at the counter. Every amount is whole paise: ₹1 is 100 paise.

export interface Payment {
  orderId: string;
  mode: "CASH" | "UPI" | "CARD";
  paise: number;
}

export function modeLabel(mode: "CASH" | "UPI" | "CARD"): string {
  // TODO 1: the label for the receipt: "Cash", "UPI" or "Card"
  return "";
}

export function orderLabel(id: string | number): string {
  // TODO 2: 17 and "17" both give "order-17". Text that already starts with "order-" stays as it is.
  return "";
}

export function totalFor(payments: Payment[], mode: "CASH" | "UPI" | "CARD"): number {
  // TODO 3: add up the paise of the payments made with that mode
  return 0;
}

export function dayTotals(payments: Payment[]): { CASH: number; UPI: number; CARD: number } {
  // TODO 4: the total paise for each mode. A mode with no payments stays at 0.
  return { CASH: 0, UPI: 0, CARD: 0 };
}

export function readMode(typed: string): "CASH" | "UPI" | "CARD" | "OTHER" {
  // TODO 5: trim and upper-case what Raju typed. Return it when it is "CASH", "UPI" or "CARD", and "OTHER" otherwise.
  return "OTHER";
}
