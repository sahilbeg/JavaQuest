import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { StockBadge, ItemCard } from "../src/ItemCard.jsx";

afterEach(cleanup);

describe("StockBadge", () => {
  it("says Sold out when stock is 0", () => {
    const { container } = render(<StockBadge stock={0} />);
    const badge = container.querySelector("span.badge.sold-out");
    expect(badge, "stock 0 should show <span className=\"badge sold-out\">").not.toBeNull();
    expect(badge.textContent, "the sold-out badge should say Sold out").toBe("Sold out");
  });

  it("warns Only 2 left when stock is 2", () => {
    const { container } = render(<StockBadge stock={2} />);
    const badge = container.querySelector("span.badge.low");
    expect(badge, "stock 2 should show <span className=\"badge low\">").not.toBeNull();
    expect(badge.textContent, "the low-stock badge should say Only 2 left").toBe("Only 2 left");
  });

  it("still warns at exactly 3 left", () => {
    render(<StockBadge stock={3} />);
    expect(screen.queryByText("Only 3 left"), "stock 3 should show Only 3 left").not.toBeNull();
  });

  it("shows nothing when there are more than 3", () => {
    const { container } = render(<StockBadge stock={4} />);
    expect(container.innerHTML, "with 4 in stock, StockBadge should return null").toBe("");
  });
});

describe("ItemCard", () => {
  it("offers Add to cart while the item is in stock", () => {
    render(<ItemCard name="Indigo tote bag" paise={89900} stock={7} onAdd={vi.fn()} onNotify={vi.fn()} />);
    expect(screen.queryByRole("button", { name: "Add to cart" }), "with 7 in stock, the card should offer Add to cart").not.toBeNull();
    expect(screen.queryByRole("button", { name: "Notify me" }), "with 7 in stock, there should be no Notify me button").toBeNull();
  });

  it("offers Notify me instead of Add to cart when sold out", async () => {
    const user = userEvent.setup();
    const onNotify = vi.fn();
    render(<ItemCard name="Golu the elephant" paise={45000} stock={0} onAdd={vi.fn()} onNotify={onNotify} />);
    expect(screen.queryByRole("button", { name: "Add to cart" }), "a sold-out card must not offer Add to cart").toBeNull();
    const notify = screen.queryByRole("button", { name: "Notify me" });
    expect(notify, "a sold-out card should offer a Notify me button").not.toBeNull();
    await user.click(notify);
    expect(onNotify.mock.calls.length, "pressing Notify me should call onNotify once").toBe(1);
  });

  it("shows the stock badge inside the card", () => {
    const { container } = render(<ItemCard name="Mint keychain" paise={12000} stock={1} onAdd={vi.fn()} onNotify={vi.fn()} />);
    const badge = container.querySelector("article.item-card span.badge");
    expect(badge, "the card should show a StockBadge when stock is low").not.toBeNull();
    expect(badge.textContent, "one keychain left should say Only 1 left").toBe("Only 1 left");
  });

  it("mentions gift wrap only when giftWrap is true", () => {
    const { container, rerender } = render(
      <ItemCard name="Indigo tote bag" paise={89900} stock={7} giftWrap={true} onAdd={vi.fn()} onNotify={vi.fn()} />,
    );
    expect(container.querySelector("p.gift-wrap")?.textContent, "giftWrap={true} should show Gift wrap available").toBe("Gift wrap available");
    rerender(<ItemCard name="Indigo tote bag" paise={89900} stock={7} onAdd={vi.fn()} onNotify={vi.fn()} />);
    expect(container.querySelector("p.gift-wrap"), "without giftWrap, there should be no gift wrap line").toBeNull();
  });

  it("never shows a stray 0 when the item is sold out", () => {
    render(<ItemCard name="Golu the elephant" paise={45000} stock={0} onAdd={vi.fn()} onNotify={vi.fn()} />);
    expect(screen.queryByText("0"), "a sold-out card should not show a lonely 0: check what is left of each &&").toBeNull();
  });
});
