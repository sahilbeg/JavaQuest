// The data layer for orders: the only code that may touch the "orders" table.
import { dbAll, dbFind, dbInsert, nextOrderNumber } from "./db.js";
import { findItem } from "./catalogue.js";
import { rupees } from "./money.js";

/** What a page may see of an order. */
export function toOrderView(row) {
  return {
    id: row.id,
    customer: row.name,
    phoneEnding: row.phone.slice(-4),
    total: rupees(row.totalPaise),
    status: row.status,
  };
}

const orderNumber = (id) => Number(id.split("-")[1]);

/** Views of all orders, newest first, optionally only those with one status. */
export function getOrders({ status } = {}) {
  return dbAll("orders")
    .filter((row) => !status || row.status === status)
    .sort((a, b) => orderNumber(b.id) - orderNumber(a.id))
    .map(toOrderView);
}

/** The view of one order, or null. */
export function getOrderView(id) {
  const row = dbFind("orders", id);
  return row ? toOrderView(row) : null;
}

/** Prices the lines on the server, saves a NEW order and returns its view. */
export function createOrder({ name, phone, lines }) {
  let items = 0;
  for (const line of lines) {
    const item = findItem(line.itemId);
    if (!item) throw new Error(`Unknown item ${line.itemId}.`);
    items += item.paise * line.qty;
  }
  const delivery = items > 99900 ? 0 : 6000;
  const row = {
    id: `order-${nextOrderNumber()}`,
    name,
    phone,
    lines: lines.map(({ itemId, qty }) => ({ itemId, qty })),
    totalPaise: items + delivery,
    status: "NEW",
    upiRef: null,
  };
  dbInsert("orders", row);
  return toOrderView(row);
}
