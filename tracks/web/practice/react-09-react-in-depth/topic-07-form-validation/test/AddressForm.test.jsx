import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AddressForm } from "../src/AddressForm.jsx";

const MESSAGES = [
  "Please enter your name.",
  "Phone needs 10 digits, like 98765 43210.",
  "Please enter your house and street.",
  "PIN code needs 6 digits.",
];

afterEach(cleanup);

/** Fills in every field with a good address. */
async function fillGoodAddress(user) {
  await user.type(screen.getByLabelText("Name"), "Asha Rao ");
  await user.type(screen.getByLabelText("Phone"), "98765 43210");
  await user.type(screen.getByLabelText("House and street"), "12 Mint Lane, Aundh");
  await user.type(screen.getByLabelText("PIN code"), "411007");
}

describe("AddressForm", () => {
  it("shows no errors before the customer tries to save", () => {
    render(<AddressForm onSubmit={vi.fn()} />);
    for (const message of MESSAGES) {
      expect(screen.queryByText(message), `"${message}" should not show before Save is pressed`).toBeNull();
    }
  });

  it("shows every problem when an empty form is saved, and doesn't submit", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<AddressForm onSubmit={onSubmit} />);
    await user.click(screen.getByRole("button", { name: "Save address" }));
    for (const message of MESSAGES) {
      expect(screen.queryByText(message), `after saving an empty form, "${message}" should show`).not.toBeNull();
    }
    expect(onSubmit.mock.calls.length, "an invalid form must not call onSubmit").toBe(0);
  });

  it("links an error message to its field for screen readers", async () => {
    const user = userEvent.setup();
    render(<AddressForm onSubmit={vi.fn()} />);
    const pin = screen.getByLabelText("PIN code");
    expect(pin.getAttribute("aria-invalid"), "before saving, the PIN field should have no aria-invalid").toBeNull();
    await user.click(screen.getByRole("button", { name: "Save address" }));
    expect(pin.getAttribute("aria-invalid"), "a PIN field with an error should have aria-invalid=\"true\"").toBe("true");
    const describedBy = pin.getAttribute("aria-describedby");
    const message = describedBy ? document.getElementById(describedBy) : null;
    expect(message?.textContent, "aria-describedby should point at the element holding the PIN code message").toBe(
      "PIN code needs 6 digits.",
    );
  });

  it("removes a message as soon as its field is fixed", async () => {
    const user = userEvent.setup();
    render(<AddressForm onSubmit={vi.fn()} />);
    await user.click(screen.getByRole("button", { name: "Save address" }));
    await user.type(screen.getByLabelText("PIN code"), "411007");
    expect(screen.queryByText("PIN code needs 6 digits."), "after typing a good PIN code its message should go away").toBeNull();
    expect(screen.queryByText("Please enter your name."), "the other messages should stay until their fields are fixed").not.toBeNull();
    expect(screen.getByLabelText("PIN code").getAttribute("aria-invalid"), "the fixed PIN field should lose aria-invalid").toBeNull();
  });

  it("refuses a phone number that is one digit short", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<AddressForm onSubmit={onSubmit} />);
    await fillGoodAddress(user);
    await user.type(screen.getByLabelText("Phone"), "{Backspace}");
    await user.click(screen.getByRole("button", { name: "Save address" }));
    expect(screen.queryByText("Phone needs 10 digits, like 98765 43210."), "a 9-digit phone should show the phone message").not.toBeNull();
    expect(onSubmit.mock.calls.length, "the form should not be sent with a 9-digit phone").toBe(0);
  });

  it("sends a good address once, cleaned up", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<AddressForm onSubmit={onSubmit} />);
    await fillGoodAddress(user);
    await user.click(screen.getByRole("button", { name: "Save address" }));
    expect(onSubmit.mock.calls.length, "a good address should call onSubmit once").toBe(1);
    expect(onSubmit.mock.calls[0][0], "the name should be trimmed and the phone should have no spaces").toEqual({
      name: "Asha Rao",
      phone: "9876543210",
      line1: "12 Mint Lane, Aundh",
      pincode: "411007",
    });
  });
});
