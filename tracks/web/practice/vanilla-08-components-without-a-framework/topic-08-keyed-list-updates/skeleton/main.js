// The packing screen. The checks never load this file.
import { drawQueue } from "./orders.js";

const queue = document.querySelector("#queue");
const ITEMS = ["2 × Golu the elephant", "1 × Indigo tote bag", "4 × Marigold coaster set", "1 × Rani pink keychain"];
let nextNumber = 20;
let orders = [
  { id: "order-17", text: "2 × Golu the elephant", status: "new" },
  { id: "order-18", text: "1 × Indigo tote bag", status: "paid" },
  { id: "order-19", text: "4 × Marigold coaster set", status: "paid" },
];

function show(next) {
  orders = next;
  drawQueue(queue, orders);
}

document.querySelector("#new-order").addEventListener("click", () => {
  const id = `order-${nextNumber}`;
  show([...orders, { id, text: ITEMS[nextNumber % ITEMS.length], status: "new" }]);
  nextNumber += 1;
});

document.querySelector("#pay-first").addEventListener("click", () => {
  show(orders.map((order, index) => (index === 0 ? { ...order, status: "paid" } : order)));
});

document.querySelector("#shipped").addEventListener("click", () => {
  show(orders.slice(1));
});

show(orders);
