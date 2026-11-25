import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App } from "../src/App.tsx";

afterEach(cleanup);

/** Finds a button by its accessible name, with a friendly message when it is missing. */
function button(name: string, why: string) {
  const found = screen.queryByRole("button", { name });
  expect(found, why).not.toBeNull();
  return found as HTMLElement;
}

/** Finds a form field by its label, with a friendly message when it is missing. */
function field(label: string, why: string) {
  const found = screen.queryByLabelText(label);
  expect(found, why).not.toBeNull();
  return found as HTMLElement;
}

describe("Stage 5: the whole store", () => {
  it("starts with a skip link, then has a banner, a named menu, a main area and a footer", async () => {
    const user = userEvent.setup();
    render(<App />);
    await user.tab();
    expect(document.activeElement?.textContent, "the first Tab should land on a \"Skip to content\" link (TODO 9)").toBe("Skip to content");
    expect(document.activeElement?.getAttribute("href"), "the skip link should go to #main").toBe("#main");
    expect(screen.queryByRole("banner"), "the page needs a <header>").not.toBeNull();
    expect(screen.queryByRole("navigation", { name: "Main" }), "the menu should be a <nav> with aria-label=\"Main\" (TODO 9)").not.toBeNull();
    expect(screen.queryByRole("main"), "the page needs a <main>").not.toBeNull();
    expect(screen.queryByRole("contentinfo"), "the page needs a <footer>").not.toBeNull();
  });

  it("counts every item in the header", async () => {
    const user = userEvent.setup();
    render(<App />);
    expect(screen.queryByText("Cart: 0 items"), "the header should start with \"Cart: 0 items\"").not.toBeNull();
    const addGolu = button("Add Golu the elephant to cart", "the catalogue needs an Add button for Golu (stage 2)");
    await user.click(addGolu);
    expect(screen.queryByText("Cart: 1 item"), "one Golu should show \"Cart: 1 item\" (TODO 9 and TODO 10)").not.toBeNull();
    await user.click(addGolu);
    expect(screen.queryByText("Cart: 2 items"), "adding Golu again should show \"Cart: 2 items\" (TODO 10)").not.toBeNull();
  });

  it("sells Golu and a tote bag, from search to UPI, end to end", async () => {
    const user = userEvent.setup();
    render(<App />);

    await user.type(field("Search", "the catalogue needs a Search box"), "golu");
    await user.click(button("Add Golu the elephant to cart", "searching \"golu\" should leave Golu's Add button"));
    await user.clear(field("Search", "the Search box should still be there"));
    await user.selectOptions(field("Category", "the catalogue needs a Category select"), "Accessories");
    await user.click(button("Add Rani pink tote bag to cart", "the Accessories category should show the tote bag"));

    await user.selectOptions(field("Quantity for Golu the elephant", "the cart should have a quantity select for Golu (stage 3)"), "2");
    expect(screen.queryByText("Cart: 3 items"), "two Golus and a tote are 3 items").not.toBeNull();
    expect(screen.queryByText("Diwali offer: -₹179.90"), "179900 paise is above ₹1,500, so 17990 paise come off").not.toBeNull();
    expect(screen.queryByText("Delivery: Free"), "the order is above ₹999, so delivery is free").not.toBeNull();
    expect(screen.queryByText("Total: ₹1,619.10"), "the total should be ₹1,619.10").not.toBeNull();

    await user.click(button("Checkout", "the cart needs a Checkout button"));
    expect(screen.queryByRole("heading", { name: "Delivery address" }), "Checkout should open the address step (TODO 10)").not.toBeNull();
    await user.type(field("Name", "checkout needs a Name field"), "Meera Kulkarni");
    await user.type(field("Phone", "checkout needs a Phone field"), "98765 43210");
    await user.type(field("PIN code", "checkout needs a PIN code field"), "411007");
    await user.click(button("Continue to payment", "the address step needs a Continue to payment button"));

    await user.type(field("UPI ID", "the payment step needs a UPI ID field"), "meera@okaxis");
    await user.click(button("Pay ₹1,619.10", "the pay button should show the total, \"Pay ₹1,619.10\""));
    expect(screen.queryByText("Order order-17 is placed."), "the order should be placed").not.toBeNull();
    expect(screen.queryByText("Cart: 0 items"), "after the order is placed, the cart should be empty again (TODO 10)").not.toBeNull();
  });
});
