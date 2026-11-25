/** The most of one item a customer can order. */
export const MAX_QTY = 5;

/** Returns the new cart lines for an action. Never changes the lines it is given. */
export function cartReducer(lines, action) {
  // TODO 5: return NEW arrays and objects, never change lines:
  //   { type: "add", product }  a new product becomes { id, name, paise, qty: 1 } (no other fields), added at the end;
  //                             a product already in the cart gets 1 more, but never more than MAX_QTY
  //   { type: "setQty", id, qty }  that line's qty becomes qty
  //   { type: "remove", id }       that line is dropped
  //   { type: "clear" }            no lines at all
  //   anything else                the same lines
  return lines;
}
