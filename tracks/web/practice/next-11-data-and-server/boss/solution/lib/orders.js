import { countOrders, insertOrder, nextOrderNumber } from "./db.js";
import { findItem } from "./catalogue.js";

/** Prices the lines, saves a NEW order and returns { id, totalPaise }. */
export async function createOrder({ name, phone, lines }) {
  let items = 0;
  for (const line of lines) {
    const item = findItem(line.itemId);
    if (!item) throw new Error(`Unknown item ${line.itemId}.`);
    items += item.paise * line.qty;
  }
  const totalPaise = items + (items > 99900 ? 0 : 6000);

  const id = `order-${await nextOrderNumber()}`;
  await insertOrder({ id, name, phone, lines, totalPaise, status: "NEW" });
  return { id, totalPaise };
}
