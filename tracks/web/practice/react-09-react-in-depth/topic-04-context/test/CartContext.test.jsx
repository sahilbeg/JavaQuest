import { describe, it, expect, afterEach, beforeEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CartProvider, useCartContext } from "../src/CartContext.jsx";
import { Header } from "../src/Header.jsx";
import { AddButton } from "../src/AddButton.jsx";
import { Shop } from "../src/Shop.jsx";

const KEY = { code: "KEY", name: "Keychain", paise: 12000 };

beforeEach(() => {
  localStorage.clear();
});

afterEach(cleanup);

describe("The shared cart", () => {
  it("starts the header at Cart (0)", () => {
    render(<Shop />);
    expect(screen.queryByText("Cart (0)"), "an empty shop should show Cart (0) in the header").not.toBeNull();
  });

  it("updates the header when a button deep in the page adds an item", async () => {
    const user = userEvent.setup();
    render(<Shop />);
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    await user.click(screen.getByRole("button", { name: "Add Coaster set" }));
    expect(
      screen.queryByText("Cart (3)"),
      "after 2 keychains and 1 coaster set the header should say Cart (3): the buttons and the header must share one cart",
    ).not.toBeNull();
  });

  it("shows the total from the same cart", async () => {
    const user = userEvent.setup();
    render(<Shop />);
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    await user.click(screen.getByRole("button", { name: "Add Coaster set" }));
    expect(screen.queryByText("Total: ₹390.00"), "2 x ₹120 + ₹150 should show Total: ₹390.00").not.toBeNull();
  });

  it("gives each provider its own cart", async () => {
    const user = userEvent.setup();
    render(
      <>
        <CartProvider>
          <Header />
          <AddButton item={KEY} />
        </CartProvider>
        <CartProvider>
          <Header />
        </CartProvider>
      </>,
    );
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    expect(screen.queryAllByText("Cart (1)").length, "the header next to the button should say Cart (1)").toBe(1);
    expect(screen.queryAllByText("Cart (0)").length, "the header in the other provider should still say Cart (0)").toBe(1);
  });

  it("gives a clear error when used outside a CartProvider", () => {
    function Probe() {
      try {
        useCartContext();
        return <p>no error</p>;
      } catch (error) {
        return <p>{error.message}</p>;
      }
    }
    render(<Probe />);
    expect(
      screen.queryByText("useCartContext must be used inside <CartProvider>"),
      "with no provider above, useCartContext should throw an Error with that message",
    ).not.toBeNull();
  });
});
