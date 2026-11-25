import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Checkout } from "../src/Checkout.tsx";

afterEach(cleanup);

const LINES = [
  { code: "CST", name: "Coaster set", paise: 15000, qty: 2 },
  { code: "AMI", name: "Golu the elephant", paise: 45000, qty: 1 },
];

function heading() {
  return screen.queryByRole("heading", { level: 2 })?.textContent;
}

/** Walks from the cart to the UPI step with Asha's address. */
async function toUpi(user: ReturnType<typeof userEvent.setup>) {
  await user.click(screen.getByRole("button", { name: "Continue to address" }));
  await user.type(screen.getByLabelText("Name"), " Asha Rao ");
  await user.type(screen.getByLabelText("Phone"), "98765 43210");
  await user.type(screen.getByLabelText("PIN code"), "411007");
  await user.click(screen.getByRole("button", { name: "Continue to payment" }));
}

describe("Checkout", () => {
  it("starts on the cart, with the totals", () => {
    render(<Checkout lines={LINES} placeOrder={() => "order-17"} />);
    expect(heading(), "checkout should start on Your cart").toBe("Your cart");
    expect(screen.queryByText("Coaster set × 2: ₹300.00"), "each line should show name × qty: price").not.toBeNull();
    expect(screen.queryByText("Delivery: ₹60.00"), "₹750 of items pays ₹60 delivery").not.toBeNull();
    expect(screen.queryByText("To pay: ₹810.00"), "the total should be ₹810.00").not.toBeNull();
  });

  it("won't continue with an empty cart", () => {
    render(<Checkout lines={[]} placeOrder={() => "order-17"} />);
    expect(screen.queryByText("Your cart is empty."), "an empty cart should say so").not.toBeNull();
    expect((screen.getByRole("button", { name: "Continue to address" }) as HTMLButtonElement).disabled, "Continue should be disabled").toBe(true);
  });

  it("goes from the cart to the address step", async () => {
    const user = userEvent.setup();
    render(<Checkout lines={LINES} placeOrder={() => "order-17"} />);
    await user.click(screen.getByRole("button", { name: "Continue to address" }));
    expect(heading(), "Continue to address should show Delivery address (use useReducer)").toBe("Delivery address");
  });

  it("only moves to payment with a valid address", async () => {
    const user = userEvent.setup();
    render(<Checkout lines={LINES} placeOrder={() => "order-17"} />);
    await user.click(screen.getByRole("button", { name: "Continue to address" }));
    await user.click(screen.getByRole("button", { name: "Continue to payment" }));
    expect(heading(), "an empty address must not reach payment").toBe("Delivery address");
    expect(screen.queryByText("PIN code needs 6 digits."), "the PIN code message should show after the first try").not.toBeNull();
  });

  it("shows the amount and the address on the UPI step", async () => {
    const user = userEvent.setup();
    render(<Checkout lines={LINES} placeOrder={() => "order-17"} />);
    await toUpi(user);
    expect(heading(), "a valid address should lead to Pay by UPI").toBe("Pay by UPI");
    expect(screen.queryByText("Pay ₹810.00 to dhaaga@upi"), "the UPI step should show the amount to pay").not.toBeNull();
    expect(screen.queryByText("Delivering to Asha Rao, 411007"), "the UPI step should show the cleaned name and PIN code").not.toBeNull();
  });

  it("keeps the address when you go back from payment", async () => {
    const user = userEvent.setup();
    render(<Checkout lines={LINES} placeOrder={() => "order-17"} />);
    await toUpi(user);
    await user.click(screen.getByRole("button", { name: "Back" }));
    expect(heading(), "Back from payment should show the address step").toBe("Delivery address");
    expect((screen.getByLabelText("Name") as HTMLInputElement).value, "the saved name should be filled in again").toBe("Asha Rao");
    expect((screen.getByLabelText("PIN code") as HTMLInputElement).value, "the saved PIN code should be filled in again").toBe("411007");
  });

  it("places the order once and shows its id", async () => {
    const user = userEvent.setup();
    const placeOrder = vi.fn(() => "order-17");
    render(<Checkout lines={LINES} placeOrder={placeOrder} />);
    await toUpi(user);
    await user.click(screen.getByRole("button", { name: "I've paid" }));
    expect(placeOrder.mock.calls.length, "I've paid should place the order exactly once").toBe(1);
    expect(placeOrder.mock.calls[0]?.[0], "placeOrder should get the lines, the cleaned address and the total").toEqual({
      lines: LINES,
      address: { name: "Asha Rao", phone: "9876543210", pincode: "411007" },
      total: 81000,
    });
    expect(heading(), "after paying, the page should say Order placed").toBe("Order placed");
    expect(screen.queryByText("Your order id is order-17."), "the order id from placeOrder should be shown").not.toBeNull();
  });
});
