import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ProductCard } from "../src/ProductCard.jsx";

afterEach(cleanup);

const TOTE = { id: "DC-1045", code: "BAG", name: "Indigo tote bag", paise: 89900, colour: "Indigo" };

describe("ProductCard", () => {
  it("shows the name, the price in rupees and the colour", () => {
    const { container } = render(<ProductCard product={TOTE} onAdd={vi.fn()} />);
    expect(container.querySelector("article.product-card h3")?.textContent, "the <h3> should show the name").toBe("Indigo tote bag");
    expect(container.querySelector("p.price")?.textContent, "89900 paise should show as ₹899.00 in p.price").toBe("₹899.00");
    expect(container.querySelector("p.colour")?.textContent, "p.colour should show the colour").toBe("Indigo");
  });

  it("reports the product's id when Add is pressed", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn();
    render(<ProductCard product={TOTE} onAdd={onAdd} />);
    const add = screen.queryByRole("button", { name: "Add Indigo tote bag to cart" });
    expect(add, "the card needs a button labelled Add Indigo tote bag to cart").not.toBeNull();
    expect(add.textContent, "the button's text should be Add").toBe("Add");
    await user.click(add);
    expect(onAdd.mock.calls.length, "pressing Add should call onAdd once").toBe(1);
    expect(onAdd.mock.calls[0][0], "onAdd should get the id DC-1045").toBe("DC-1045");
  });
});
