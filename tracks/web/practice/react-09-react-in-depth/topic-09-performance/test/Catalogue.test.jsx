import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Catalogue } from "../src/Catalogue.jsx";
import { ProductRow, rowRenders } from "../src/ProductRow.jsx";
import { sortStats } from "../src/sortByPrice.js";
import { ITEMS } from "../src/items.js";

beforeEach(() => {
  rowRenders.length = 0;
  sortStats.runs = 0;
});

afterEach(cleanup);

/** The Add buttons' text, top to bottom. */
function buttonOrder() {
  return screen.getAllByRole("button", { name: /^Add / }).map((button) => button.textContent);
}

describe("Catalogue", () => {
  it("lists every item, cheapest first", () => {
    render(<Catalogue items={ITEMS} />);
    expect(buttonOrder(), "the rows should go from the ₹120 keychain up to the ₹899 tote bag").toEqual([
      "Add Keychain",
      "Add Coaster set",
      "Add Baby booties",
      "Add Amigurumi elephant",
      "Add Tote bag",
    ]);
  });

  it("puts the dearest first when you choose High to low", async () => {
    const user = userEvent.setup();
    render(<Catalogue items={ITEMS} />);
    await user.selectOptions(screen.getByLabelText("Sort by price"), "high-first");
    expect(buttonOrder()[0], "with High to low, the tote bag should come first").toBe("Add Tote bag");
  });

  it("counts what you add to the cart", async () => {
    const user = userEvent.setup();
    render(<Catalogue items={ITEMS} />);
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    expect(screen.queryByText("Cart (2)"), "two taps on Add should show Cart (2)").not.toBeNull();
  });

  it("draws no product row again when you add to the cart", async () => {
    const user = userEvent.setup();
    render(<Catalogue items={ITEMS} />);
    rowRenders.length = 0;
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    expect(
      rowRenders,
      "no row's props changed, so no row should render again (use memo on ProductRow and useCallback for handleAdd)",
    ).toEqual([]);
  });

  it("doesn't sort again when you add to the cart", async () => {
    const user = userEvent.setup();
    render(<Catalogue items={ITEMS} />);
    const before = sortStats.runs;
    await user.click(screen.getByRole("button", { name: "Add Keychain" }));
    expect(sortStats.runs, "adding to the cart changes neither items nor order, so the list shouldn't be sorted again (useMemo)").toBe(
      before,
    );
  });

  it("sorts once more when the order changes", async () => {
    const user = userEvent.setup();
    render(<Catalogue items={ITEMS} />);
    const before = sortStats.runs;
    await user.selectOptions(screen.getByLabelText("Sort by price"), "high-first");
    expect(sortStats.runs - before, "changing the order should sort exactly once more").toBe(1);
  });
});

describe("ProductRow", () => {
  it("skips rendering when it gets the very same props", () => {
    const onAdd = vi.fn();
    const { rerender } = render(<ProductRow item={ITEMS[1]} onAdd={onAdd} />);
    rerender(<ProductRow item={ITEMS[1]} onAdd={onAdd} />);
    expect(rowRenders.length, "with the same item and the same onAdd, the second render should be skipped (memo)").toBe(1);
  });
});
