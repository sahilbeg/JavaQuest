import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup, waitFor, act } from "@testing-library/react";
import { Suspense } from "react";
import { SalesCard, LowStockCard } from "../src/app/dashboard/cards.jsx";

afterEach(cleanup);

describe("SalesCard", () => {
  it("shows today's orders and takings from its promise", async () => {
    await act(async () => {
      render(
      <Suspense fallback={<p>waiting</p>}>
        <SalesCard salesPromise={Promise.resolve({ orders: 12, totalPaise: 845000 })} />
      </Suspense>,
      );
    });
    await waitFor(() =>
      expect(screen.queryByText("12 orders, ₹8,450.00"), "12 orders worth 845000 paise should read 12 orders, ₹8,450.00 (use(salesPromise))").not.toBeNull(),
    );
    expect(screen.queryByRole("heading", { name: "Today's sales" }), "the card should have the heading Today's sales").not.toBeNull();
  });
});

describe("LowStockCard", () => {
  it("lists each item running low", async () => {
    const items = [
      { id: "DC-1042", name: "Golu the elephant", left: 2 },
      { id: "DC-3007", name: "Marigold tote bag", left: 3 },
    ];
    await act(async () => {
      render(
      <Suspense fallback={<p>waiting</p>}>
        <LowStockCard stockPromise={Promise.resolve(items)} />
      </Suspense>,
      );
    });
    await waitFor(() => expect(screen.queryByText("Golu the elephant: 2 left"), "Golu should be listed as Golu the elephant: 2 left").not.toBeNull());
    expect(screen.queryAllByRole("listitem").length, "two items running low, two <li>").toBe(2);
    expect(screen.queryByRole("heading", { name: "Low stock" }), "the card should have the heading Low stock").not.toBeNull();
  });

  it("says so when nothing is running low", async () => {
    await act(async () => {
      render(
      <Suspense fallback={<p>waiting</p>}>
        <LowStockCard stockPromise={Promise.resolve([])} />
      </Suspense>,
      );
    });
    await waitFor(() => expect(screen.queryByText("Nothing is running low."), "an empty list should say Nothing is running low.").not.toBeNull());
    expect(screen.queryByRole("list"), "an empty list shouldn't draw an empty <ul>").toBeNull();
  });
});
