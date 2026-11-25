// The packing screen's rows. An order looks like { id: "order-17", text: "2 × Golu the elephant", status: "paid" }.
import { reconcile } from "./keyed.js";

/** A new, empty row: a Packed checkbox, the order's text and its status. */
export function createOrderRow() {
  const row = document.createElement("li");
  row.className = "order";
  row.innerHTML = `
    <label><input type="checkbox" class="packed"> <span class="what"></span></label>
    <span class="status"></span>`;
  return row;
}

/** Fills a row from its order. Runs for every row on every update, so it never touches the checkbox. */
export function updateOrderRow(row, order) {
  row.querySelector(".what").textContent = `${order.id}: ${order.text}`;
  row.querySelector(".status").textContent = order.status;
  row.classList.toggle("is-new", order.status === "new");
}

/** Draws the packing queue, keeping rows that are already there. */
export function drawQueue(list, orders) {
  reconcile(list, orders, { key: (order) => order.id, create: createOrderRow, update: updateOrderRow });
}
