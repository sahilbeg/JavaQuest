// Reserving stock for an order, with errors when it can't be done.

export class OutOfStockError extends Error {
  constructor(item, qty) {
    super("Only " + item.stock + " of " + item.id + " left, but " + qty + " were asked for");
    this.name = "OutOfStockError";
    this.itemId = item.id;
  }
}

export function reserve(item, qty) {
  if (!Number.isInteger(qty) || qty < 1) {
    throw new RangeError("qty must be a whole number above 0");
  }
  if (qty > item.stock) {
    throw new OutOfStockError(item, qty);
  }
  return { ...item, stock: item.stock - qty };
}

export function tryReserve(item, qty) {
  try {
    return { ok: true, item: reserve(item, qty) };
  } catch (error) {
    if (error instanceof OutOfStockError) {
      return { ok: false, message: error.message };
    }
    throw error;
  }
}
