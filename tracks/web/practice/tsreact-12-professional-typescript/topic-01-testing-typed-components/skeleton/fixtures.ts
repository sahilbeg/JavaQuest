import type { Order, OrderLine, OrderStatus } from "./types.ts";

/** One Golu the elephant: the line most tests need. */
export const GOLU_LINE: OrderLine = { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 1 };

/** A fresh, complete order for a test. Pass only the fields the test cares about. */
export function makeOrder(overrides: Partial<Order> = {}): Order {
  // TODO 1: return order-17 for Meera Kulkarni, status "placed", with one copy of GOLU_LINE, then spread overrides on top. Every call needs its own lines array and its own line object.
  return { id: "", customer: "", status: "placed", lines: [] };
}

/** What the customer should see for one status. */
export interface StatusCase {
  label: string;
  canCancel: boolean;
  canTrack: boolean;
}

/** One row per status. Record<OrderStatus, ...> makes the compiler ask for a row for every status. */
export const STATUS_CASES: Record<OrderStatus, StatusCase> = {
  // TODO 2: fill in each row from the table in the README: the label, whether Cancel order shows, and whether Track parcel shows.
  placed: { label: "", canCancel: false, canTrack: false },
  packed: { label: "", canCancel: false, canTrack: false },
  shipped: { label: "", canCancel: false, canTrack: false },
  delivered: { label: "", canCancel: false, canTrack: false },
  cancelled: { label: "", canCancel: false, canTrack: false },
};

/** A home-made, typed spy: fn remembers the arguments of every call in calls. */
export interface Spy<Args extends unknown[]> {
  fn: (...args: Args) => void;
  calls: Args[];
}

export function makeSpy<Args extends unknown[]>(): Spy<Args> {
  // TODO 3: make one calls array, and a fn that pushes its arguments (...args: Args) into it. Return both.
  return { fn: () => {}, calls: [] };
}
