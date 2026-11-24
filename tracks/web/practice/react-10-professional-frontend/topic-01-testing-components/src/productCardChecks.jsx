import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, vi } from "vitest";

/** The product every check uses: Golu the elephant, ₹450, 7 in stock. */
export const GOLU = { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 7 };

// Worked example: render the card, then look at the page the way a customer would.
export async function checkShowsPrice(Card) {
  render(<Card product={GOLU} onAdd={vi.fn()} />);
  expect(screen.queryByText("₹450.00"), "Golu costs 45000 paise, so the card should show ₹450.00").not.toBeNull();
}

export async function checkAddsOne(Card) {
  // TODO 1: render <Card product={GOLU} onAdd={onAdd} /> with onAdd = vi.fn(), click "Add to cart" with userEvent, and expect onAdd.mock.calls to equal [["DC-1042", 1]].
}

export async function checkAddsChosenQuantity(Card) {
  // TODO 2: render Golu's card, pick "3" in the select labelled "Quantity" (find it by its label), click "Add to cart", and expect onAdd.mock.calls to equal [["DC-1042", 3]].
}

export async function checkSoldOut(Card) {
  // TODO 3: render Golu with stock: 0. Expect the text "Out of stock", a disabled "Add to cart" button, and no onAdd call after clicking it.
}
