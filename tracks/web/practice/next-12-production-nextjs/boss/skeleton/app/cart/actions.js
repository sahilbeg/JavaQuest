"use server";

import { getItem } from "../../lib/catalogue.js";

// Round 2, the server action round: "Write addToCart(prevState, formData) for useActionState."
// The state is { lines: [{ id, qty }], error: "" }. The candidate's answer:
export async function addToCart(prevState, formData) {
  // TODO 2: check the input. An unknown id gives the error "Unknown item"; a qty that isn't a whole number from 1 to 5
  // gives "Pick a quantity from 1 to 5"; more than the stock gives "Only 7 left" (or "Sold out" when stock is 0).
  // On any error, return the old lines unchanged with the error.
  // TODO 3: qty arrives as text, and this changes prevState in place. Turn qty into a number, add it to an existing line,
  // and return brand-new arrays and objects, leaving prevState exactly as it was.
  const id = formData.get("id");
  const qty = formData.get("qty");
  const existing = prevState.lines.find((line) => line.id === id);
  if (existing) {
    existing.qty += qty;
  } else {
    prevState.lines.push({ id, qty });
  }
  return { lines: prevState.lines, error: "" };
}
