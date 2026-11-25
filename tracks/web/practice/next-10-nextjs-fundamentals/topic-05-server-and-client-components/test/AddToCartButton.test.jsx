import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AddToCartButton } from "../src/app/AddToCartButton.jsx";
import { source, startsWithUseClient } from "./source.js";

afterEach(cleanup);

describe("AddToCartButton", () => {
  it("is a client component", () => {
    expect(
      startsWithUseClient(source("app/AddToCartButton.jsx")),
      "AddToCartButton.jsx should start with the line \"use client\";, above its imports",
    ).toBe(true);
  });

  it("shows no cart message before it is pressed", () => {
    render(<AddToCartButton itemId="DC-1042" name="Golu the elephant" />);
    expect(screen.queryByRole("button", { name: "Add to cart" }), "there should be a button that says Add to cart").not.toBeNull();
    expect(screen.queryByText(/in your cart/), "before any press, nothing should say ... in your cart").toBeNull();
  });

  it("counts each press", async () => {
    const user = userEvent.setup();
    render(<AddToCartButton itemId="DC-1042" name="Golu the elephant" />);
    const button = screen.getByRole("button", { name: "Add to cart" });
    await user.click(button);
    expect(screen.queryByText("1 in your cart"), "after one press, the button should be followed by 1 in your cart").not.toBeNull();
    await user.click(button);
    expect(screen.queryByText("2 in your cart"), "after two presses, it should say 2 in your cart").not.toBeNull();
  });
});
