import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";

vi.mock("next/cache", () => ({ revalidatePath: vi.fn(), revalidateTag: vi.fn(), updateTag: vi.fn() }));

import OrdersPage from "../src/app/orders/page.jsx";
import { resetOrders, createOrder, setStatus, getOrder } from "../src/lib/orders.js";

/** Changes order-20's status, if createOrder saved it. */
function order20Is(status) {
  if (getOrder("order-20")) setStatus("order-20", status);
}

beforeEach(() => {
  resetOrders();
});

afterEach(cleanup);

/** Two orders: order-20 (Asha, ₹510.00) and order-21 (Ravi, ₹760.00). */
function twoOrders() {
  createOrder({ name: "Asha Rao", phone: "9876543210", lines: [{ itemId: "DC-1042", qty: 1 }] });
  createOrder({ name: "Ravi Joshi", phone: "9876543211", lines: [{ itemId: "DC-4410", qty: 2 }] });
}

describe("OrdersPage", () => {
  it("lists every order, newest first", async () => {
    twoOrders();
    render(await OrdersPage());
    const lines = screen.queryAllByRole("listitem").map((li) => li.querySelector("p")?.textContent);
    expect(lines, "one <li> per order, newest first, each with a <p> like order-20 · Asha Rao · ₹510.00 · NEW").toEqual([
      "order-21 · Ravi Joshi · ₹760.00 · NEW",
      "order-20 · Asha Rao · ₹510.00 · NEW",
    ]);
  });

  it("gives each order a button for its next step, with a hidden orderId", async () => {
    twoOrders();
    order20Is("SHIPPED");
    const { container } = render(await OrdersPage());
    expect(screen.queryByRole("button", { name: "Move to PACKED" }), "the NEW order-21 should have a Move to PACKED button").not.toBeNull();
    expect(screen.queryByRole("button", { name: "Move to DELIVERED" }), "the SHIPPED order-20 should have a Move to DELIVERED button").not.toBeNull();
    const hidden = [...container.querySelectorAll('input[type="hidden"][name="orderId"]')].map((input) => input.value);
    expect(hidden, "each form needs <input type=\"hidden\" name=\"orderId\" value={order.id} />").toEqual(["order-21", "order-20"]);
  });

  it("shows no button for a delivered order", async () => {
    twoOrders();
    order20Is("DELIVERED");
    render(await OrdersPage());
    expect(screen.queryAllByRole("button").length, "only order-21 can still move, so there should be one button").toBe(1);
  });

  it("says so when there are no orders", async () => {
    render(await OrdersPage());
    expect(screen.queryByText("No orders yet."), "with no orders, the page should say No orders yet.").not.toBeNull();
  });
});
