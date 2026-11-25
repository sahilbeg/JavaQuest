import type { Order, OrderLine, OrderStatus } from "./types.ts";

/** One Golu the elephant: the line most tests need. */
export const GOLU_LINE: OrderLine = { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 1 };

/** A fresh, complete order for a test. Pass only the fields the test cares about. */
export function makeOrder(overrides: Partial<Order> = {}): Order {
  return {
    id: "order-17",
    customer: "Meera Kulkarni",
    status: "placed",
    lines: [{ ...GOLU_LINE }],
    ...overrides,
  };
}

/** What the customer should see for one status. */
export interface StatusCase {
  label: string;
  canCancel: boolean;
  canTrack: boolean;
}

/** One row per status. Record<OrderStatus, ...> makes the compiler ask for a row for every status. */
export const STATUS_CASES: Record<OrderStatus, StatusCase> = {
  placed: { label: "Order placed", canCancel: true, canTrack: false },
  packed: { label: "Packed, waiting for the courier", canCancel: true, canTrack: false },
  shipped: { label: "On its way", canCancel: false, canTrack: true },
  delivered: { label: "Delivered", canCancel: false, canTrack: false },
  cancelled: { label: "Cancelled", canCancel: false, canTrack: false },
};

/** A home-made, typed spy: fn remembers the arguments of every call in calls. */
export interface Spy<Args extends unknown[]> {
  fn: (...args: Args) => void;
  calls: Args[];
}

export function makeSpy<Args extends unknown[]>(): Spy<Args> {
  const calls: Args[] = [];
  return {
    fn: (...args: Args) => {
      calls.push(args);
    },
    calls,
  };
}
