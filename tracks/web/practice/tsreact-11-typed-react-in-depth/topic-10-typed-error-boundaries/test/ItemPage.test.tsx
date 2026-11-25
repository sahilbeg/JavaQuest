import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ItemPage } from "../src/ItemPage.tsx";

beforeEach(() => {
  vi.spyOn(console, "error").mockImplementation(() => {});
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

const BROKEN = [{ id: "DC-2001", name: "Marigold coaster set" }, { id: "DC-3005" }];
const FINE = [{ id: "DC-1042", name: "Golu the elephant" }];

describe("ItemPage", () => {
  it("shows the suggestions when they are fine", () => {
    render(<ItemPage itemId="DC-2001" name="Marigold coaster set" price="₹150.00" similar={FINE} onAdd={() => {}} />);
    expect(screen.queryByText("Golu the elephant"), "a good suggestion list should show its items").not.toBeNull();
  });

  it("keeps the page when a suggestion is broken", () => {
    render(<ItemPage itemId="DC-1042" name="Golu the elephant" price="₹450.00" similar={BROKEN} onAdd={() => {}} />);
    expect(screen.queryByRole("heading", { level: 1, name: "Golu the elephant" }), "Golu's heading should survive a broken suggestion").not.toBeNull();
    expect(screen.queryByText("₹450.00"), "the price should still show").not.toBeNull();
    expect(screen.queryByRole("alert")?.textContent, "the suggestions should be replaced by the fallback").toContain(
      "Similar items are taking a break.",
    );
  });

  it("still lets the customer add Golu to the cart", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn();
    render(<ItemPage itemId="DC-1042" name="Golu the elephant" price="₹450.00" similar={BROKEN} onAdd={onAdd} />);
    await user.click(screen.getByRole("button", { name: "Add to cart" }));
    expect(onAdd.mock.calls.length, "Add to cart must stay outside the boundary and keep working").toBe(1);
  });

  it("shows the next item's suggestions after moving on from a broken one", () => {
    const { rerender } = render(<ItemPage itemId="DC-1042" name="Golu the elephant" price="₹450.00" similar={BROKEN} onAdd={() => {}} />);
    rerender(<ItemPage itemId="DC-2001" name="Marigold coaster set" price="₹150.00" similar={FINE} onAdd={() => {}} />);
    expect(screen.queryByRole("alert"), "a new itemId should clear the fallback (pass resetKey={itemId})").toBeNull();
    expect(screen.queryByText("Golu the elephant"), "the coaster set's suggestions should show").not.toBeNull();
  });
});
