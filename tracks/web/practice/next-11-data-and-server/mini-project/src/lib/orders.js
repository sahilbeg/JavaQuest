// The data layer for orders, kept in memory. Only this file touches the orders list.
import { findItem } from "./catalogue.js";

/** Where each status goes next. A DELIVERED order is finished. */
export const NEXT_STATUS = { NEW: "PACKED", PACKED: "SHIPPED", SHIPPED: "DELIVERED" };

let orders = [];
let nextNumber = 20;

/** Empties the orders. The checks call this before each test. */
export function resetOrders() {
  orders = [];
  nextNumber = 20;
}

/** A copy of the order with this id, or undefined. */
export function getOrder(id) {
  const order = orders.find((o) => o.id === id);
  return order ? { ...order } : undefined;
}

/** Copies of every order, newest first. */
export function listOrders() {
  return [...orders].reverse().map((order) => ({ ...order }));
}

/** Changes the status of an order that exists. */
export function setStatus(id, status) {
  orders.find((o) => o.id === id).status = status;
}

/** Prices the lines from the catalogue, saves a NEW order and returns a copy. */
export function createOrder({ name, phone, lines }) {
  // TODO 1: price each line with findItem(line.itemId).paise * line.qty.
  // Unknown item: throw new Error(`Unknown item ${line.itemId}.`) before saving anything.
  // Delivery: 6000 paise, free when the items come to more than 99900.
  // Save { id: `order-${nextNumber}`, name, phone, lines (only itemId and qty), totalPaise, status: "NEW" },
  // add 1 to nextNumber, and return a copy.
  return null;
}
