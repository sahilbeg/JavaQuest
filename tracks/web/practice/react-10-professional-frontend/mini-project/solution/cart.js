/** The most of one item a customer can buy in one order. */
export const MAX_QTY = 5;

/**
 * The cart's reducer. lines is [{ id, name, paise, qty }].
 * Actions: { type: "add", product }, { type: "setQty", id, qty }, { type: "remove", id }, { type: "clear" }.
 */
export function cartReducer(lines, action) {
  switch (action.type) {
    case "add": {
      const { id, name, paise } = action.product;
      if (lines.some((line) => line.id === id)) {
        return lines.map((line) => (line.id === id ? { ...line, qty: Math.min(line.qty + 1, MAX_QTY) } : line));
      }
      return [...lines, { id, name, paise, qty: 1 }];
    }
    case "setQty":
      return lines.map((line) => (line.id === action.id ? { ...line, qty: action.qty } : line));
    case "remove":
      return lines.filter((line) => line.id !== action.id);
    case "clear":
      return [];
    default:
      return lines;
  }
}
