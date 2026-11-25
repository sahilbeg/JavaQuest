import type { CartAction, CartLine } from "./types.ts";

/** The most of one item in one order. */
export const MAX_QTY = 5;

/** The cart's reducer. It never changes the lines it is given: every action returns new lines. */
export function cartReducer(lines: CartLine[], action: CartAction): CartLine[] {
  // TODO 4: switch on action.type.
  // "add": a new product becomes { id, name, paise, qty: 1 } at the end; a product already in the cart gets 1 more, never above MAX_QTY.
  // "setQty" sets that line's qty, "remove" drops that line, "clear" empties the cart.
  // In the default branch, const unhandled: never = action; makes a forgotten action a type error.
  return lines;
}
