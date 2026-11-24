import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Checkout } from "../src/Checkout.jsx";

afterEach(cleanup);

const LINES = [{ id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 1 }];

function setup() {
  const user = userEvent.setup();
  const onPlaced = vi.fn();
  render(<Checkout lines={LINES} orderId="order-17" onPlaced={onPlaced} onBack={vi.fn()} />);
  return { user, onPlaced };
}

async function fillAddress(user, { name, phone, pin }) {
  await user.type(screen.getByLabelText("Name"), name);
  await user.type(screen.getByLabelText("Phone"), phone);
  await user.type(screen.getByLabelText("PIN code"), pin);
  await user.click(screen.getByRole("button", { name: "Continue to payment" }));
}

const heading = (name) => screen.queryByRole("heading", { name });
const alertText = () => screen.queryByRole("alert")?.textContent;
const GOOD = { name: "Meera Kulkarni", phone: "98765 43210", pin: "411007" };

describe("Stage 4: Checkout", () => {
  it("starts on the address step, marked as the current step", () => {
    setup();
    expect(heading("Delivery address"), "checkout should start with the \"Delivery address\" heading").not.toBeNull();
    expect(screen.queryByText("Address").getAttribute("aria-current"), "the Address step should have aria-current=\"step\"").toBe("step");
  });

  it("moves focus to the heading when checkout opens", () => {
    setup();
    expect(document.activeElement === heading("Delivery address"), "opening checkout should focus its heading, so a screen reader starts there (TODO 8)").toBe(true);
  });

  it("refuses a short PIN code with a message", async () => {
    const { user } = setup();
    await fillAddress(user, { ...GOOD, pin: "4110" });
    expect(alertText(), "a 4-digit PIN code should show the alert \"Enter a 6-digit PIN code.\" (TODO 6)").toBe("Enter a 6-digit PIN code.");
    expect(heading("Delivery address"), "with a bad PIN code, checkout should stay on the address step").not.toBeNull();
  });

  it("refuses a short phone number, and an empty name", async () => {
    const { user } = setup();
    await fillAddress(user, { ...GOOD, phone: "98765" });
    expect(alertText(), "a 5-digit phone number should show \"Enter a 10-digit phone number.\" (TODO 6)").toBe("Enter a 10-digit phone number.");
    cleanup();
    const second = setup();
    await second.user.click(screen.getByRole("button", { name: "Continue to payment" }));
    expect(alertText(), "an empty form should first ask \"Enter your name.\" (TODO 6)").toBe("Enter your name.");
  });

  it("moves to the UPI step, shows the amount, and focuses its heading", async () => {
    const { user } = setup();
    await fillAddress(user, GOOD);
    const payHeading = heading("Pay by UPI");
    expect(payHeading, "a good address should move on to \"Pay by UPI\"").not.toBeNull();
    expect(document.activeElement === payHeading, "focus should move to the new step's heading (TODO 8)").toBe(true);
    expect(screen.queryByText("Amount to pay: ₹510.00"), "Golu plus ₹60 delivery is ₹510.00").not.toBeNull();
    expect(screen.queryByText("Payment").getAttribute("aria-current"), "the Payment step should now be the current step").toBe("step");
    expect(alertText(), "no alert should show after a good address").toBe(undefined);
  });

  it("refuses a UPI ID without a bank", async () => {
    const { user, onPlaced } = setup();
    await fillAddress(user, GOOD);
    await user.type(screen.getByLabelText("UPI ID"), "meera");
    await user.click(screen.getByRole("button", { name: "Pay ₹510.00" }));
    expect(alertText(), "\"meera\" has no @bank, so show \"Enter a UPI ID like name@bank.\" (TODO 7)").toBe("Enter a UPI ID like name@bank.");
    expect(onPlaced.mock.calls.length, "a bad UPI ID must not place the order").toBe(0);
  });

  it("places the order with a good UPI ID", async () => {
    const { user, onPlaced } = setup();
    await fillAddress(user, GOOD);
    await user.type(screen.getByLabelText("UPI ID"), "meera@okaxis");
    await user.click(screen.getByRole("button", { name: "Pay ₹510.00" }));
    expect(onPlaced.mock.calls, "paying should call onPlaced once with the order id, the total in paise and UPI (TODO 7)").toEqual([
      [{ orderId: "order-17", totalPaise: 51000, payment: "UPI" }],
    ]);
    expect(heading("Thank you!"), "after paying, the heading should say \"Thank you!\"").not.toBeNull();
    expect(screen.queryByText("Order order-17 is placed."), "the thank-you should name the order").not.toBeNull();
  });
});
