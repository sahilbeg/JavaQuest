import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Checkout } from "../src/Checkout.jsx";

const LINES = [
  { code: "CST", name: "Coaster set", paise: 15000, qty: 2 },
  { code: "AMI", name: "Amigurumi elephant", paise: 45000, qty: 1 },
];

afterEach(cleanup);

/** From the cart, fills in Asha's address and goes on to the UPI step. */
async function reachPayment(user) {
  await user.click(screen.getByRole("button", { name: "Continue to address" }));
  await user.type(screen.getByLabelText("Name"), "Asha Rao");
  await user.type(screen.getByLabelText("Phone"), "98765 43210");
  await user.type(screen.getByLabelText("PIN code"), "411007");
  await user.click(screen.getByRole("button", { name: "Continue to payment" }));
}

describe("Checkout", () => {
  it("shows the cart's lines, the delivery charge and the amount to pay", () => {
    render(<Checkout lines={LINES} placeOrder={vi.fn()} />);
    expect(screen.queryByText("Coaster set × 2: ₹300.00"), "each line should read like Coaster set × 2: ₹300.00").not.toBeNull();
    expect(screen.queryByText("Items: ₹750.00"), "the items come to ₹750.00").not.toBeNull();
    expect(screen.queryByText("Delivery: ₹60.00"), "an order of ₹750 pays ₹60 delivery").not.toBeNull();
    expect(screen.queryByText("To pay: ₹810.00"), "the amount to pay is ₹810.00").not.toBeNull();
  });

  it("says delivery is free above ₹999", () => {
    const bigOrder = [
      { code: "BAG", name: "Tote bag", paise: 89900, qty: 1 },
      { code: "CST", name: "Coaster set", paise: 15000, qty: 1 },
    ];
    render(<Checkout lines={bigOrder} placeOrder={vi.fn()} />);
    expect(screen.queryByText("Delivery: Free"), "a ₹1,049 order should show Delivery: Free").not.toBeNull();
    expect(screen.queryByText("To pay: ₹1,049.00"), "the amount to pay should be ₹1,049.00").not.toBeNull();
  });

  it("won't start checkout with an empty cart", () => {
    render(<Checkout lines={[]} placeOrder={vi.fn()} />);
    expect(screen.queryByText("Your cart is empty."), "an empty cart should say Your cart is empty.").not.toBeNull();
    expect(screen.getByRole("button", { name: "Continue to address" }).disabled, "Continue to address should be disabled").toBe(true);
  });

  it("stays on the address step until the address is valid", async () => {
    const user = userEvent.setup();
    render(<Checkout lines={LINES} placeOrder={vi.fn()} />);
    await user.click(screen.getByRole("button", { name: "Continue to address" }));
    expect(screen.queryByRole("heading", { name: "Delivery address" }), "Continue should open the Delivery address step").not.toBeNull();
    await user.click(screen.getByRole("button", { name: "Continue to payment" }));
    expect(screen.queryByText("PIN code needs 6 digits."), "an empty address should show the PIN code message").not.toBeNull();
    expect(screen.queryByRole("heading", { name: "Pay by UPI" }), "a bad address must not reach the payment step").toBeNull();
  });

  it("takes the customer from cart to order id, placing the order once", async () => {
    const user = userEvent.setup();
    const placeOrder = vi.fn(() => "order-17");
    render(<Checkout lines={LINES} placeOrder={placeOrder} />);
    await reachPayment(user);
    expect(screen.queryByText("Pay ₹810.00 to dhaaga@upi"), "the UPI step should say Pay ₹810.00 to dhaaga@upi").not.toBeNull();
    expect(screen.queryByText("Delivering to Asha Rao, 411007"), "the UPI step should repeat who and where it's going to").not.toBeNull();
    expect(placeOrder.mock.calls.length, "nothing should be ordered before I've paid is pressed").toBe(0);

    await user.click(screen.getByRole("button", { name: "I've paid" }));
    expect(placeOrder.mock.calls.length, "I've paid should place the order exactly once").toBe(1);
    expect(placeOrder.mock.calls[0][0], "placeOrder should get the lines, the cleaned address and the total in paise").toEqual({
      lines: LINES,
      address: { name: "Asha Rao", phone: "9876543210", pincode: "411007" },
      total: 81000,
    });
    expect(screen.queryByText("Your order id is order-17."), "the last step should show Your order id is order-17.").not.toBeNull();
  });

  it("keeps the address when the customer goes back from payment", async () => {
    const user = userEvent.setup();
    render(<Checkout lines={LINES} placeOrder={vi.fn()} />);
    await reachPayment(user);
    await user.click(screen.getByRole("button", { name: "Back" }));
    expect(screen.queryByRole("heading", { name: "Delivery address" }), "Back should return to the address step").not.toBeNull();
    expect(screen.getByLabelText("Name").value, "the name typed before should still be there").toBe("Asha Rao");
    expect(screen.getByLabelText("PIN code").value, "the PIN code typed before should still be there").toBe("411007");
  });
});
