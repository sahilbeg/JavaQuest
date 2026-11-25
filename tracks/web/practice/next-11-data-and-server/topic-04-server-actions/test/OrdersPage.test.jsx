import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";

vi.mock("next/cache", () => ({ revalidatePath: vi.fn(), revalidateTag: vi.fn(), updateTag: vi.fn() }));

import OrdersPage from "../src/app/orders/page.jsx";
import { resetOrders } from "../src/lib/orders.js";

beforeEach(() => {
  resetOrders();
});

afterEach(cleanup);

describe("OrdersPage", () => {
  it("shows every order with its status", async () => {
    render(await OrdersPage());
    expect(screen.queryByText("order-17 · Asha Rao · ₹410.00 · NEW"), "order-17 should be listed as NEW").not.toBeNull();
    expect(screen.queryByText("order-18 · Ravi Joshi · ₹510.00 · PACKED"), "order-18 should be listed as PACKED").not.toBeNull();
  });

  it("gives each new order's Mark packed form a hidden orderId field", async () => {
    const { container } = render(await OrdersPage());
    const hidden = [...container.querySelectorAll('input[name="orderId"]')];
    expect(hidden.map((input) => input.value), "the two NEW orders, order-17 and order-19, each need <input type=\"hidden\" name=\"orderId\" value={order.id} />").toEqual(["order-17", "order-19"]);
    expect(hidden.every((input) => input.type === "hidden"), "the orderId field should be type=\"hidden\", so Raju never sees it").toBe(true);
  });

  it("shows the buttons only for new orders", async () => {
    render(await OrdersPage());
    expect(screen.queryAllByRole("button", { name: "Mark packed" }).length, "two NEW orders, two Mark packed buttons").toBe(2);
    expect(screen.queryAllByRole("button", { name: "Cancel" }).length, "two NEW orders, two Cancel buttons").toBe(2);
  });
});
