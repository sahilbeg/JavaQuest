import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { OrderHistory } from "../src/OrderHistory.tsx";
import { OrderRow, rowRenders } from "../src/OrderRow.tsx";
import { ORDERS, filterStats } from "../src/orders.ts";

beforeEach(() => {
  rowRenders.length = 0;
  filterStats.runs = 0;
});

afterEach(cleanup);

/** The ids of the orders shown, top to bottom, read from their Open buttons. */
function shownIds() {
  return screen.queryAllByRole("button", { name: /^Open / }).map((button) => button.textContent?.replace("Open ", ""));
}

describe("OrderHistory", () => {
  it("lists every order at first", () => {
    render(<OrderHistory orders={ORDERS} />);
    expect(shownIds(), "all five orders should be listed").toEqual(["order-17", "order-18", "order-19", "order-20", "order-21"]);
  });

  it("shows only the orders that match the search", async () => {
    const user = userEvent.setup();
    render(<OrderHistory orders={ORDERS} />);
    await user.type(screen.getByLabelText("Search orders"), "asha");
    expect(shownIds(), "searching asha should leave Asha Rao's two orders").toEqual(["order-17", "order-19"]);
  });

  it("says which order you opened", async () => {
    const user = userEvent.setup();
    render(<OrderHistory orders={ORDERS} />);
    await user.click(screen.getByRole("button", { name: "Open order-18" }));
    expect(screen.queryByText("Opened order-18"), "after tapping Open on order-18 the page should say Opened order-18").not.toBeNull();
  });

  it("draws no row again when you open an order", async () => {
    const user = userEvent.setup();
    render(<OrderHistory orders={ORDERS} />);
    rowRenders.length = 0;
    await user.click(screen.getByRole("button", { name: "Open order-17" }));
    expect(
      rowRenders,
      "no row's props changed, so no row should render again (use memo on OrderRow and useCallback for handleOpen)",
    ).toEqual([]);
  });

  it("doesn't search again when you open an order", async () => {
    const user = userEvent.setup();
    render(<OrderHistory orders={ORDERS} />);
    const before = filterStats.runs;
    await user.click(screen.getByRole("button", { name: "Open order-17" }));
    expect(filterStats.runs, "opening an order changes neither orders nor query, so filterOrders shouldn't run again (useMemo)").toBe(before);
  });

  it("searches once more for each letter you type", async () => {
    const user = userEvent.setup();
    render(<OrderHistory orders={ORDERS} />);
    const before = filterStats.runs;
    await user.type(screen.getByLabelText("Search orders"), "17");
    expect(filterStats.runs - before, "typing two letters should search exactly twice").toBe(2);
  });
});

describe("OrderRow", () => {
  it("skips rendering when it gets the very same props", () => {
    const onOpen = vi.fn();
    const { rerender } = render(
      <ul>
        <OrderRow order={ORDERS[0]} onOpen={onOpen} />
      </ul>,
    );
    rerender(
      <ul>
        <OrderRow order={ORDERS[0]} onOpen={onOpen} />
      </ul>,
    );
    expect(rowRenders.length, "with the same order and the same onOpen, the second render should be skipped (memo)").toBe(1);
  });
});
