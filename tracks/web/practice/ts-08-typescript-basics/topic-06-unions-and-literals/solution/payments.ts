// The day's payments at the counter. Every amount is whole paise: ₹1 is 100 paise.

export interface Payment {
  orderId: string;
  mode: "CASH" | "UPI" | "CARD";
  paise: number;
}

export function modeLabel(mode: "CASH" | "UPI" | "CARD"): string {
  if (mode === "CASH") {
    return "Cash";
  }
  if (mode === "UPI") {
    return "UPI";
  }
  return "Card";
}

export function orderLabel(id: string | number): string {
  const text = String(id);
  return text.startsWith("order-") ? text : "order-" + text;
}

export function totalFor(payments: Payment[], mode: "CASH" | "UPI" | "CARD"): number {
  let total = 0;
  for (const payment of payments) {
    if (payment.mode === mode) {
      total += payment.paise;
    }
  }
  return total;
}

export function dayTotals(payments: Payment[]): { CASH: number; UPI: number; CARD: number } {
  const totals = { CASH: 0, UPI: 0, CARD: 0 };
  for (const payment of payments) {
    totals[payment.mode] += payment.paise;
  }
  return totals;
}

export function readMode(typed: string): "CASH" | "UPI" | "CARD" | "OTHER" {
  const mode = typed.trim().toUpperCase();
  if (mode === "CASH" || mode === "UPI" || mode === "CARD") {
    return mode;
  }
  return "OTHER";
}
