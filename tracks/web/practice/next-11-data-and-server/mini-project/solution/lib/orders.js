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
  let items = 0;
  for (const line of lines) {
    const item = findItem(line.itemId);
    if (!item) throw new Error(`Unknown item ${line.itemId}.`);
    items += item.paise * line.qty;
  }
  const delivery = items > 99900 ? 0 : 6000;
  const order = {
    id: `order-${nextNumber}`,
    name,
    phone,
    lines: lines.map(({ itemId, qty }) => ({ itemId, qty })),
    totalPaise: items + delivery,
    status: "NEW",
  };
  nextNumber += 1;
  orders.push(order);
  return { ...order };
}
