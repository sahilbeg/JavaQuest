// Payments at the counter, as a discriminated union: the mode says which fields a payment has.

export type Payment =
  | { mode: "CASH"; paise: number; tenderedPaise: number }
  | { mode: "UPI"; paise: number; upiRef: string }
  | { mode: "CARD"; paise: number; last4: string };

/** ₹ with two decimals, for example ₹1,299.00. Done for you. */
export function rupees(paise: number): string {
  const [whole, fraction] = (paise / 100).toFixed(2).split(".");
  return `₹${whole.replace(/\B(?=(\d{3})+(?!\d))/g, ",")}.${fraction}`;
}

/** Throws a clear error for a value the types say can't exist. Done for you. */
export function assertNever(value: never): never {
  throw new Error(`Unknown payment: ${JSON.stringify(value)}`);
}

/**
 * The receipt line for one payment:
 *   CASH -> "Cash ₹450.00, change ₹50.00"
 *   UPI  -> "UPI ₹450.00, ref UPI-5521"
 *   CARD -> "Card ₹899.00, ending 4242"
 * A mode the code doesn't know throws (through assertNever).
 */
export function receiptLine(payment: Payment): string {
  switch (payment.mode) {
    case "CASH":
      return `Cash ${rupees(payment.paise)}, change ${rupees(payment.tenderedPaise - payment.paise)}`;
    case "UPI":
      return `UPI ${rupees(payment.paise)}, ref ${payment.upiRef}`;
    case "CARD":
      return `Card ${rupees(payment.paise)}, ending ${payment.last4}`;
    default:
      return assertNever(payment);
  }
}

/** The change to hand back: tenderedPaise - paise for CASH, and 0 for UPI and CARD. */
export function changePaise(payment: Payment): number {
  return payment.mode === "CASH" ? payment.tenderedPaise - payment.paise : 0;
}

/** The day's total for each mode. Every mode is there, with 0 when nobody used it. */
export function totalsByMode(payments: Payment[]): Record<Payment["mode"], number> {
  const totals: Record<Payment["mode"], number> = { CASH: 0, UPI: 0, CARD: 0 };
  for (const payment of payments) {
    totals[payment.mode] += payment.paise;
  }
  return totals;
}
