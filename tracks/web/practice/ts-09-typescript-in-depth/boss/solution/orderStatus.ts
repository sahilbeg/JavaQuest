// The order tracker's status logic. RETURNED was added on Monday.

export type OrderStatus =
  | { kind: "PLACED" }
  | { kind: "PACKED"; packedBy: string }
  | { kind: "SHIPPED"; courier: string }
  | { kind: "DELIVERED"; deliveredOn: string }
  | { kind: "CANCELLED"; reason: string }
  | { kind: "RETURNED"; refundPaise: number };

/** ₹ with two decimals, for example ₹1,299.00. */
export function rupees(paise: number): string {
  const [whole, fraction] = (paise / 100).toFixed(2).split(".");
  return `₹${whole.replace(/\B(?=(\d{3})+(?!\d))/g, ",")}.${fraction}`;
}

/** Throws a clear error for a status the code doesn't know. */
export function assertNever(status: never): never {
  throw new Error(`Unknown order status: ${(status as { kind: string }).kind}`);
}

/** The text the order tracker shows for a status. */
export function statusLabel(status: OrderStatus): string {
  switch (status.kind) {
    case "PLACED":
      return "Order placed";
    case "PACKED":
      return `Packed by ${status.packedBy}`;
    case "SHIPPED":
      return `On the way with ${status.courier}`;
    case "DELIVERED":
      return `Delivered on ${status.deliveredOn}`;
    case "CANCELLED":
      return `Cancelled: ${status.reason}`;
    case "RETURNED":
      return `Returned, refund of ${rupees(status.refundPaise)} on its way`;
    default:
      return assertNever(status);
  }
}

/** Whether the tracker shows the Cancel button for this status. */
export function canCancel(status: OrderStatus): boolean {
  switch (status.kind) {
    case "PLACED":
    case "PACKED":
      return true;
    case "SHIPPED":
    case "DELIVERED":
    case "CANCELLED":
    case "RETURNED":
      return false;
    default:
      return assertNever(status);
  }
}
