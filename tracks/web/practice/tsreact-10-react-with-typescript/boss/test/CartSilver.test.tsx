import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Cart } from "../src/Cart.tsx";

afterEach(cleanup);

function deshpandeCart() {
  return [
    { id: "DC-1042", qty: 1 },
    { id: "DC-1031", qty: 2 },
  ];
}

describe("INC-10 silver: tidying up the cart", () => {
  it("keeps the real items' names, totals and Remove buttons", () => {
    const { container } = render(<Cart initialLines={deshpandeCart()} />);
    const golu = [...container.querySelectorAll("li.cart-line")].find((li) => !li.classList.contains("missing"));
    expect(golu?.querySelector(".name")?.textContent, "Golu's line should read Golu the elephant × 1").toBe("Golu the elephant × 1");
    expect(golu?.querySelector(".line-total")?.textContent, "Golu's line total should be ₹450.00").toBe("₹450.00");
    expect(screen.queryByRole("button", { name: "Remove Golu the elephant" }), "Golu's line should keep its Remove button").not.toBeNull();
  });

  it("lets the customer remove the missing item", async () => {
    const user = userEvent.setup();
    const { container } = render(<Cart initialLines={deshpandeCart()} />);
    const remove = screen.queryByRole("button", { name: "Remove DC-1031" });
    expect(remove, "the missing line needs a button labelled Remove DC-1031").not.toBeNull();
    await user.click(remove!);
    expect(container.querySelectorAll("li.missing").length, "after Remove, the missing line should be gone").toBe(0);
    expect(container.querySelectorAll("li.cart-line").length, "Golu's line should still be there").toBe(1);
  });

  it("counts only the items that can still be bought", () => {
    const { container } = render(<Cart initialLines={deshpandeCart()} />);
    expect(container.querySelector(".count")?.textContent, "one Golu can be bought; the two retired keychains don't count").toBe("1 item");
  });
});
