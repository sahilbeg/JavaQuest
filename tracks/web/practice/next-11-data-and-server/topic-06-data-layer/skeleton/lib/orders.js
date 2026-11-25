// The data layer for orders: the only code that may touch the "orders" table.
import { dbAll, dbFind, dbInsert, nextOrderNumber } from "./db.js";
import { findItem } from "./catalogue.js";
import { rupees } from "./money.js";

/** What a page may see of an order. */
export function toOrderView(row) {
  // TODO 1: return a new object with exactly id, customer (row.name), phoneEnding (last 4 digits of row.phone),
  // total (rupees(row.totalPaise)) and status. Never spread the row.
  return null;
}

/** Views of all orders, newest first, optionally only those with one status. */
export function getOrders({ status } = {}) {
  // TODO 2: dbAll("orders"), keep only the status asked for (if any),
  // sort by the number in the id, biggest first, and map each row with toOrderView.
  return [];
}

/** The view of one order, or null. */
export function getOrderView(id) {
  // TODO 3: use dbFind("orders", id).
  return null;
}

/** Prices the lines on the server, saves a NEW order and returns its view. */
export function createOrder({ name, phone, lines }) {
  // TODO 4: price each line with findItem(line.itemId).paise * line.qty, ignoring any price in the line.
  // Unknown item: throw new Error(`Unknown item ${line.itemId}.`) before saving anything.
  // Delivery: 6000 paise, free when the items come to more than 99900.
  // dbInsert a row { id: `order-${nextOrderNumber()}`, name, phone, lines (only itemId and qty),
  // totalPaise, status: "NEW", upiRef: null }, and return toOrderView(row).
  return null;
}
