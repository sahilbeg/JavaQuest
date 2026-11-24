import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Cart } from "../src/Cart.jsx";

afterEach(cleanup);

// Mrs. Rao's cart: one Golu, two pairs of baby booties and three Mint keychains.
function mrsRaosCart() {
  return [
    { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 1 },
    { id: "DC-1046", name: "Baby booties", paise: 35000, qty: 2 },
    { id: "DC-1044", name: "Mint keychain", paise: 12000, qty: 3 },
  ];
}

/** The row (<li>) that shows this item name. */
function rowFor(container, name) {
  return [...container.querySelectorAll("li.cart-row")].find((li) => li.querySelector(".name")?.textContent === name);
}

describe("INC-08 bronze: removing a row", () => {
  it("takes the removed item off the list", async () => {
    const user = userEvent.setup();
    const { container } = render(<Cart initialItems={mrsRaosCart()} />);
    await user.click(screen.getByRole("button", { name: "Remove Baby booties" }));
    expect(container.querySelectorAll("li.cart-row").length, "two rows should be left after removing the booties").toBe(2);
    expect(rowFor(container, "Baby booties"), "the baby booties should be gone").toBeUndefined();
  });

  it("keeps the quantity of the item below the removed one", async () => {
    const user = userEvent.setup();
    render(<Cart initialItems={mrsRaosCart()} />);
    await user.click(screen.getByRole("button", { name: "Remove Baby booties" }));
    const keychains = screen.queryByLabelText("Quantity for Mint keychain");
    expect(keychains, "the Mint keychain row should still be there").not.toBeNull();
    expect(keychains.value, "Mrs. Rao had 3 keychains; removing the booties must not change that").toBe("3");
  });

  it("keeps the line total of the item below the removed one", async () => {
    const user = userEvent.setup();
    const { container } = render(<Cart initialItems={mrsRaosCart()} />);
    await user.click(screen.getByRole("button", { name: "Remove Baby booties" }));
    const row = rowFor(container, "Mint keychain");
    expect(row?.querySelector(".line-total")?.textContent, "3 keychains at ₹120 should still cost ₹360.00").toBe("₹360.00");
  });

  it("keeps the quantity of the item above the removed one", async () => {
    const user = userEvent.setup();
    render(<Cart initialItems={mrsRaosCart()} />);
    await user.click(screen.getByRole("button", { name: "Remove Baby booties" }));
    expect(screen.queryByLabelText("Quantity for Golu the elephant")?.value, "Golu's quantity should still be 1").toBe("1");
  });
});
