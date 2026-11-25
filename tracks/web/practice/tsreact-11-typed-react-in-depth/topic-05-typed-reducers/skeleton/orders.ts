export type OrderStatus = "new" | "packed" | "shipped" | "cancelled";

export interface Order {
  id: string;
  customer: string;
  status: OrderStatus;
}

/** Everything that can happen to the order board. */
export type OrderAction =
  | { type: "added"; id: string; customer: string }
  | { type: "packed"; id: string }
  | { type: "shipped"; id: string }
  | { type: "cancelled"; id: string };

/** Only callable with never: a type error if a case is missing, and a clear error for unknown actions at runtime. */
export function assertNever(action: never): never {
  throw new Error(`Unknown action: ${(action as { type: string }).type}`);
}

/** Raju's rule card: the next list of orders after one action. */
export function ordersReducer(orders: Order[], action: OrderAction): Order[] {
  switch (action.type) {
    case "added":
      // TODO 1: add { id, customer, status: "new" } at the end. If the id is already on the board, return orders unchanged.
      return orders;
    case "packed":
      // TODO 2: the order with action.id becomes "packed", but only if it is "new".
      return orders;
    case "shipped":
      // TODO 3: the order with action.id becomes "shipped", but only if it is "packed".
      return orders;
    case "cancelled":
      // TODO 4: the order with action.id becomes "cancelled", but only if it is "new" or "packed".
      return orders;
    default:
      // TODO 5: return assertNever(action);
      return orders;
  }
}
