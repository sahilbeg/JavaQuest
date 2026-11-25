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

/** A new list where the order with this id moves to `to`, if its status is one of `from`. */
function move(orders: Order[], id: string, from: OrderStatus[], to: OrderStatus): Order[] {
  return orders.map((order) => (order.id === id && from.includes(order.status) ? { ...order, status: to } : order));
}

/** Raju's rule card: the next list of orders after one action. */
export function ordersReducer(orders: Order[], action: OrderAction): Order[] {
  switch (action.type) {
    case "added":
      if (orders.some((order) => order.id === action.id)) return orders;
      return [...orders, { id: action.id, customer: action.customer, status: "new" }];
    case "packed":
      return move(orders, action.id, ["new"], "packed");
    case "shipped":
      return move(orders, action.id, ["packed"], "shipped");
    case "cancelled":
      return move(orders, action.id, ["new", "packed"], "cancelled");
    default:
      return assertNever(action);
  }
}
