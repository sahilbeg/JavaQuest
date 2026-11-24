// Reserving stock for an order, with errors when it can't be done.

export class OutOfStockError extends Error {
  constructor(item, qty) {
    super("Only " + item.stock + " of " + item.id + " left, but " + qty + " were asked for");
    // TODO 1: set this.name to "OutOfStockError" and this.itemId to item.id
  }
}

export function reserve(item, qty) {
  // TODO 2: if qty is not a whole number of at least 1, throw new RangeError("qty must be a whole number above 0")
  // TODO 3: if qty is more than item.stock, throw new OutOfStockError(item, qty)
  // TODO 4: otherwise return a new item, like item, with stock lowered by qty
  return null;
}

export function tryReserve(item, qty) {
  // TODO 5: try reserve(item, qty) and return { ok: true, item: newItem }.
  // Catch an OutOfStockError and return { ok: false, message: error.message }. Throw any other error again.
  return null;
}
