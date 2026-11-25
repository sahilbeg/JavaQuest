import type { CartAction, CartLine } from "./types.ts";

/** The most of one item in one order. */
export const MAX_QTY = 5;

/** The cart's reducer. It never changes the lines it is given: every action returns new lines. */
export function cartReducer(lines: CartLine[], action: CartAction): CartLine[] {
  switch (action.type) {
    case "add": {
      const { product } = action;
      if (!lines.some((line) => line.id === product.id)) {
        return [...lines, { id: product.id, name: product.name, paise: product.paise, qty: 1 }];
      }
      return lines.map((line) => (line.id === product.id ? { ...line, qty: Math.min(MAX_QTY, line.qty + 1) } : line));
    }
    case "setQty": {
      const { id, qty } = action;
      return lines.map((line) => (line.id === id ? { ...line, qty } : line));
    }
    case "remove": {
      const { id } = action;
      return lines.filter((line) => line.id !== id);
    }
    case "clear":
      return [];
    default: {
      const unhandled: never = action;
      return unhandled;
    }
  }
}
