"use server";

import { getItem } from "../../lib/catalogue.js";

// Round 2, the server action round: "Write addToCart(prevState, formData) for useActionState."
// The state is { lines: [{ id, qty }], error: "" }.
export async function addToCart(prevState, formData) {
  const item = await getItem(String(formData.get("id")));
  if (!item) {
    return { lines: prevState.lines, error: "Unknown item" };
  }
  const qty = Number(formData.get("qty"));
  if (!Number.isInteger(qty) || qty < 1 || qty > 5) {
    return { lines: prevState.lines, error: "Pick a quantity from 1 to 5" };
  }
  const existing = prevState.lines.find((line) => line.id === item.id);
  const total = (existing ? existing.qty : 0) + qty;
  if (total > item.stock) {
    return { lines: prevState.lines, error: item.stock === 0 ? "Sold out" : `Only ${item.stock} left` };
  }
  const lines = existing
    ? prevState.lines.map((line) => (line.id === item.id ? { id: line.id, qty: total } : line))
    : [...prevState.lines, { id: item.id, qty }];
  return { lines, error: "" };
}
