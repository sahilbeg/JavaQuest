/** The most of one item a customer can buy in one order. */
export const MAX_QTY = 5;

/**
 * The cart's reducer. lines is [{ id, name, paise, qty }].
 * Actions: { type: "add", product }, { type: "setQty", id, qty }, { type: "remove", id }, { type: "clear" }.
 */
export function cartReducer(lines, action) {
  switch (action.type) {
    case "add":
      // TODO 4: a new product becomes a line { id, name, paise, qty: 1 }. A product already in the cart adds 1 to its qty, never above MAX_QTY.
      return [...lines, { ...action.product, qty: 1 }];
    case "setQty":
      // TODO 4: set that line's qty to action.qty.
      return lines;
    case "remove":
      // TODO 4: drop the line with action.id.
      return lines;
    case "clear":
      return [];
    default:
      return lines;
  }
}
