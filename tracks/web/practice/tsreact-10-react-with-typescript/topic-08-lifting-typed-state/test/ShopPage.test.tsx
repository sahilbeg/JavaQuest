import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ShopPage } from "../src/ShopPage.tsx";

afterEach(cleanup);

const ITEMS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1043", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-1044", name: "Mint keychain", paise: 12000 },
];

function setup() {
  const user = userEvent.setup();
  const { container } = render(<ShopPage items={ITEMS} />);
  return {
    user,
    badge: () => container.querySelector(".cart-badge")?.textContent,
    lines: () => [...container.querySelectorAll(".cart-line .line-name")].map((span) => span.textContent).join("; "),
    total: () => container.querySelector(".cart-total")?.textContent,
    empty: () => container.querySelector(".cart-empty")?.textContent,
    addButton: (name: string) => screen.queryByRole("button", { name: `Add ${name}` })!,
  };
}

describe("ShopPage", () => {
  it("starts with Cart (0) and an empty cart box", () => {
    const { badge, empty } = setup();
    expect(badge(), "the header should start at Cart (0)").toBe("Cart (0)");
    expect(empty(), "the cart box should say Your cart is empty").toBe("Your cart is empty");
  });

  it("updates the header and the cart box when you add an item", async () => {
    const { user, badge, lines, addButton } = setup();
    await user.click(addButton("Golu the elephant"));
    expect(badge(), "after adding Golu, the header should say Cart (1)").toBe("Cart (1)");
    expect(lines(), "the cart box should show one line for Golu").toBe("Golu the elephant × 1");
  });

  it("raises the quantity when the same item is added again", async () => {
    const { user, badge, lines, addButton } = setup();
    await user.click(addButton("Golu the elephant"));
    await user.click(addButton("Golu the elephant"));
    expect(lines(), "two Golus should be one line with × 2, not two lines").toBe("Golu the elephant × 2");
    expect(badge(), "the header counts every item: Cart (2)").toBe("Cart (2)");
  });

  it("keeps lines in the order they were first added, with the right total", async () => {
    const { user, badge, lines, total, addButton } = setup();
    await user.click(addButton("Golu the elephant"));
    await user.click(addButton("Marigold coaster set"));
    await user.click(addButton("Golu the elephant"));
    expect(lines(), "Golu first, then the coaster set").toBe("Golu the elephant × 2; Marigold coaster set × 1");
    expect(badge(), "two Golus and one coaster set make Cart (3)").toBe("Cart (3)");
    expect(total(), "2 × ₹450 + ₹150 is ₹1,050.00").toBe("Total: ₹1,050.00");
  });

  it("removes a line and lowers the count", async () => {
    const { user, badge, lines, addButton } = setup();
    await user.click(addButton("Golu the elephant"));
    await user.click(addButton("Golu the elephant"));
    await user.click(addButton("Mint keychain"));
    const remove = screen.queryByRole("button", { name: "Remove Golu the elephant" });
    expect(remove, "each cart line should have a Remove button").not.toBeNull();
    await user.click(remove!);
    expect(lines(), "only the keychain should be left").toBe("Mint keychain × 1");
    expect(badge(), "the header should drop to Cart (1)").toBe("Cart (1)");
  });
});
