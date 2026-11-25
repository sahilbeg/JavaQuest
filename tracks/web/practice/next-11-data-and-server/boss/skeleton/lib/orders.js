import { countOrders, insertOrder, nextOrderNumber } from "./db.js";
import { findItem } from "./catalogue.js";

/** Prices the lines, saves a NEW order and returns { id, totalPaise }. */
export async function createOrder({ name, phone, lines }) {
  // TODO 1 (bronze): two customers ordering at the same moment must get different numbers, and both orders must be saved.
  // TODO 2 (silver): a new order must never take the number of an order that exists, even after one is deleted.
  const count = await countOrders();
  const id = `order-${17 + count}`;

  let items = 0;
  for (const line of lines) {
    const item = findItem(line.itemId);
    if (!item) throw new Error(`Unknown item ${line.itemId}.`);
    items += item.paise * line.qty;
  }
  const totalPaise = items + (items > 99900 ? 0 : 6000);

  await insertOrder({ id, name, phone, lines, totalPaise, status: "NEW" });
  return { id, totalPaise };
}
