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
  const user = userEvent.setup();
  const onAdd = vi.fn();
  render(<Card product={GOLU} onAdd={onAdd} />);
  await user.click(screen.getByRole("button", { name: "Add to cart" }));
  expect(onAdd.mock.calls, "one click should call onAdd once, with Golu's id and quantity 1").toEqual([["DC-1042", 1]]);
}

export async function checkAddsChosenQuantity(Card) {
  const user = userEvent.setup();
  const onAdd = vi.fn();
  render(<Card product={GOLU} onAdd={onAdd} />);
  await user.selectOptions(screen.getByLabelText("Quantity"), "3");
  await user.click(screen.getByRole("button", { name: "Add to cart" }));
  expect(onAdd.mock.calls, "after picking 3, one click should add 3 of Golu").toEqual([["DC-1042", 3]]);
}

export async function checkSoldOut(Card) {
  const user = userEvent.setup();
  const onAdd = vi.fn();
  render(<Card product={{ ...GOLU, stock: 0 }} onAdd={onAdd} />);
  expect(screen.queryByText("Out of stock"), "a sold-out card should say Out of stock").not.toBeNull();
  const button = screen.getByRole("button", { name: "Add to cart" });
  expect(button.disabled, "a sold-out card's Add to cart button should be disabled").toBe(true);
  await user.click(button);
  expect(onAdd.mock.calls.length, "clicking a sold-out card must not add anything").toBe(0);
}
