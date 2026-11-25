import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { PRODUCTS } from "../src/catalogue.ts";
import { cartReducer } from "../src/cart.ts";
import { Cart } from "../src/Cart.tsx";

afterEach(cleanup);

const GOLU = PRODUCTS[0];
const goluLine = (qty: number) => ({ id: "DC-1042", name: "Golu the elephant", paise: 45000, qty });
const toteLine = (qty: number) => ({ id: "DC-1301", name: "Rani pink tote bag", paise: 89900, qty });

function renderCart(lines: { id: string; name: string; paise: number; qty: number }[]) {
  const handlers = { onSetQty: vi.fn(), onRemove: vi.fn(), onCheckout: vi.fn() };
  render(<Cart lines={lines} {...handlers} />);
  return handlers;
}

const text = (words: string | RegExp) => screen.queryByText(words);

describe("Stage 3: cartReducer", () => {
  it("adds a new product as a line with quantity 1", () => {
    expect(cartReducer([], { type: "add", product: GOLU }), "a line keeps only id, name, paise and qty (TODO 4)").toEqual([goluLine(1)]);
  });

  it("adds 1 to a product that is already in the cart", () => {
    expect(cartReducer([goluLine(1)], { type: "add", product: GOLU }), "adding Golu again should make one line with qty 2, not two lines").toEqual([goluLine(2)]);
  });

  it("never goes above 5 of one item", () => {
    expect(cartReducer([goluLine(5)], { type: "add", product: GOLU }), "5 is the most of one item per order").toEqual([goluLine(5)]);
  });

  it("sets a line's quantity", () => {
    expect(cartReducer([goluLine(1), toteLine(1)], { type: "setQty", id: "DC-1042", qty: 3 }), "setQty should change only Golu's line").toEqual([goluLine(3), toteLine(1)]);
  });

  it("removes only the chosen line, and clears the cart", () => {
    expect(cartReducer([goluLine(1), toteLine(1)], { type: "remove", id: "DC-1042" }), "remove should drop Golu and keep the tote").toEqual([toteLine(1)]);
    expect(cartReducer([goluLine(1), toteLine(1)], { type: "clear" }), "clear should empty the cart").toEqual([]);
  });

  it("leaves the lines it was given unchanged", () => {
    const lines = [goluLine(1)];
    cartReducer(lines, { type: "add", product: GOLU });
    cartReducer(lines, { type: "setQty", id: "DC-1042", qty: 4 });
    expect(lines, "a reducer must return new lines, never change the old ones").toEqual([goluLine(1)]);
  });
});

describe("Stage 3: Cart", () => {
  it("says when the cart is empty, and can't check out", () => {
    renderCart([]);
    expect(text("Your cart is empty."), "an empty cart should say \"Your cart is empty.\" (TODO 5)").not.toBeNull();
    const checkout = screen.queryByRole("button", { name: "Checkout" }) as HTMLButtonElement | null;
    expect(checkout?.disabled, "Checkout should be disabled for an empty cart (TODO 5)").toBe(true);
  });

  it("shows the totals for a small order, with ₹60 delivery and no offer", () => {
    renderCart([goluLine(1)]);
    expect(text("Subtotal: ₹450.00"), "the subtotal should read \"Subtotal: ₹450.00\" (stage 1)").not.toBeNull();
    expect(text("Delivery: ₹60.00"), "a ₹450 order pays delivery: \"Delivery: ₹60.00\" (TODO 5)").not.toBeNull();
    expect(text("Total: ₹510.00"), "the total should read \"Total: ₹510.00\"").not.toBeNull();
    expect(text(/Diwali offer/), "no offer line should show below ₹1,500").toBeNull();
  });

  it("shows the Diwali offer and free delivery on a big order", () => {
    renderCart([toteLine(2)]);
    expect(text("Subtotal: ₹1,798.00"), "two totes: \"Subtotal: ₹1,798.00\"").not.toBeNull();
    expect(text("Diwali offer: -₹179.80"), "the offer line should read \"Diwali offer: -₹179.80\" (TODO 5)").not.toBeNull();
    expect(text("Delivery: Free"), "above ₹999 the delivery line should read \"Delivery: Free\" (TODO 5)").not.toBeNull();
    expect(text("Total: ₹1,618.20"), "the total should read \"Total: ₹1,618.20\"").not.toBeNull();
  });

  it("changes a quantity with a labelled select", async () => {
    const user = userEvent.setup();
    const { onSetQty } = renderCart([goluLine(1)]);
    const select = screen.queryByLabelText("Quantity for Golu the elephant");
    expect(select, "Golu's quantity select needs the label \"Quantity for Golu the elephant\" (TODO 5)").not.toBeNull();
    await user.selectOptions(select as HTMLElement, "3");
    expect(onSetQty.mock.calls, "picking 3 should call onSetQty(\"DC-1042\", 3), with a number").toEqual([["DC-1042", 3]]);
  });

  it("removes a line with a button named after the item", async () => {
    const user = userEvent.setup();
    const { onRemove } = renderCart([goluLine(1), toteLine(1)]);
    const button = screen.queryByRole("button", { name: "Remove Golu the elephant" });
    expect(button, "each Remove button needs aria-label=\"Remove <name>\" (TODO 5)").not.toBeNull();
    await user.click(button as HTMLElement);
    expect(onRemove.mock.calls, "Remove should call onRemove(\"DC-1042\")").toEqual([["DC-1042"]]);
  });

  it("opens checkout", async () => {
    const user = userEvent.setup();
    const { onCheckout } = renderCart([goluLine(1)]);
    await user.click(screen.getByRole("button", { name: "Checkout" }));
    expect(onCheckout.mock.calls.length, "Checkout should call onCheckout once").toBe(1);
  });
});
