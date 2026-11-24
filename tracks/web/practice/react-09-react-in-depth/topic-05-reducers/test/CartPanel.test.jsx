import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CartPanel } from "../src/CartPanel.jsx";

const ITEMS = [
  { code: "CST", name: "Coaster set", paise: 15000 },
  { code: "KEY", name: "Keychain", paise: 12000 },
];

afterEach(cleanup);

describe("CartPanel", () => {
  it("adds items when you press their buttons", async () => {
    const user = userEvent.setup();
    render(<CartPanel items={ITEMS} />);
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    expect(screen.queryByText("Keychain × 2"), "two presses of Add Keychain should show Keychain × 2").not.toBeNull();
    expect(screen.queryByText("Cart (2)"), "the heading should say Cart (2)").not.toBeNull();
    expect(screen.queryByText("Total: ₹240.00"), "two keychains cost ₹240.00").not.toBeNull();
  });

  it("takes one off, and drops the line at zero", async () => {
    const user = userEvent.setup();
    render(<CartPanel items={ITEMS} />);
    await user.click(screen.getByRole("button", { name: "Add Coaster set" }));
    await user.click(screen.getByRole("button", { name: "Add Coaster set" }));
    await user.click(screen.getByRole("button", { name: "One less Coaster set" }));
    expect(screen.queryByText("Coaster set × 1"), "One less should take the coaster sets from 2 to 1").not.toBeNull();
    await user.click(screen.getByRole("button", { name: "One less Coaster set" }));
    expect(screen.queryByText("Coaster set × 0"), "a line should never show qty 0").toBeNull();
    expect(screen.queryByText("Cart (0)"), "with the last coaster set gone the cart should be empty").not.toBeNull();
  });

  it("removes a line and empties the cart", async () => {
    const user = userEvent.setup();
    render(<CartPanel items={ITEMS} />);
    await user.click(screen.getByRole("button", { name: "Add Coaster set" }));
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    await user.click(screen.getByRole("button", { name: "Remove Keychain" }));
    expect(screen.queryByText("Keychain × 1"), "Remove Keychain should drop the keychain line").toBeNull();
    expect(screen.queryByText("Coaster set × 1"), "the coaster set should still be there").not.toBeNull();
    await user.click(screen.getByRole("button", { name: "Empty the cart" }));
    expect(screen.queryByText("Cart (0)"), "Empty the cart should leave Cart (0)").not.toBeNull();
  });
});
