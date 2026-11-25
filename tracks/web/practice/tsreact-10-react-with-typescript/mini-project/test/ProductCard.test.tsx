import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ProductCard, stockText } from "../src/ProductCard.tsx";

afterEach(cleanup);

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, colour: "Indigo", stock: { kind: "in-stock", count: 2 } } as const;
const keychain = { id: "DC-1044", name: "Mint keychain", paise: 12000, colour: "Mint", stock: { kind: "sold-out" } } as const;

function addButton(name: string) {
  return screen.queryByRole("button", { name: `Add ${name} to cart` }) as HTMLButtonElement | null;
}

describe("stockText", () => {
  it("says In stock, Only N left or Sold out", () => {
    expect(stockText({ kind: "in-stock", count: 12 }), "12 left is plenty: In stock").toBe("In stock");
    expect(stockText({ kind: "in-stock", count: 4 }), "4 left is still plenty: In stock").toBe("In stock");
    expect(stockText({ kind: "in-stock", count: 3 }), "3 left: Only 3 left").toBe("Only 3 left");
    expect(stockText({ kind: "sold-out" }), "sold out: Sold out").toBe("Sold out");
  });
});

describe("ProductCard", () => {
  it("shows the name, price, colour and stock", () => {
    const { container } = render(<ProductCard product={golu} inCart={0} onAdd={() => {}} />);
    const card = container.querySelector("article.product-card");
    expect(card?.querySelector("h3")?.textContent, "the <h3> should hold the name").toBe("Golu the elephant");
    expect(card?.querySelector(".price")?.textContent, "45000 paise should show as ₹450.00").toBe("₹450.00");
    expect(card?.querySelector(".colour")?.textContent, "the colour line should say Indigo").toBe("Indigo");
    expect(card?.querySelector(".stock")?.textContent, "2 in stock should say Only 2 left").toBe("Only 2 left");
  });

  it("calls onAdd with the product's id", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn();
    render(<ProductCard product={golu} inCart={0} onAdd={onAdd} />);
    const add = addButton("Golu the elephant");
    expect(add, "there should be a button labelled Add Golu the elephant to cart").not.toBeNull();
    await user.click(add!);
    expect(onAdd.mock.calls.map((call) => call[0]).join(","), "onAdd should get DC-1042").toBe("DC-1042");
  });

  it("can't add a sold-out product", () => {
    render(<ProductCard product={keychain} inCart={0} onAdd={() => {}} />);
    expect(addButton("Mint keychain")?.disabled, "Add should be disabled for the sold-out keychain").toBe(true);
  });

  it("can't add more than the stock", () => {
    render(
      <>
        <ProductCard product={golu} inCart={1} onAdd={() => {}} />
        <ProductCard product={{ ...golu, id: "DC-1099", name: "Second Golu" }} inCart={2} onAdd={() => {}} />
      </>,
    );
    expect(addButton("Golu the elephant")?.disabled, "1 in the cart and 2 in stock: Add should still work").toBe(false);
    expect(addButton("Second Golu")?.disabled, "2 in the cart and 2 in stock: Add should be disabled").toBe(true);
  });
});
