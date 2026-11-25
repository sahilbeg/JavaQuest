import { describe, it, expect, beforeEach, vi } from "vitest";

vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
  redirect: vi.fn((to) => {
    throw new Error("NEXT_REDIRECT " + to);
  }),
  useRouter: () => ({ push: vi.fn() }),
  usePathname: () => "/",
}));
vi.mock("next/cache", () => ({ revalidatePath: vi.fn(), revalidateTag: vi.fn(), updateTag: vi.fn() }));

import { revalidatePath } from "next/cache";
import { placeOrder } from "../src/app/order/actions.js";
import { resetOrders, getOrder, listOrders } from "../src/lib/orders.js";

const START = { errors: {}, values: { name: "", phone: "", itemId: "DC-1042", qty: "1" } };
const GOOD = { name: "  Asha Rao ", phone: "98765 43210", itemId: "DC-1042", qty: "1" };

function form(fields) {
  const data = new FormData();
  for (const [name, value] of Object.entries(fields)) data.append(name, value);
  return data;
}

beforeEach(() => {
  resetOrders();
  vi.clearAllMocks();
});

describe("placeOrder", () => {
  it("saves a good order and moves the customer to its page", async () => {
    await expect(placeOrder(START, form(GOOD)), "a good order should end with redirect(\"/order/order-20\")").rejects.toThrow(
      "NEXT_REDIRECT /order/order-20",
    );
    const order = getOrder("order-20");
    expect(order?.name, "the name should be saved trimmed").toBe("Asha Rao");
    expect(order?.phone, "the phone should be saved without spaces").toBe("9876543210");
    expect(order?.lines, "the order should have one line with qty as a number").toEqual([{ itemId: "DC-1042", qty: 1 }]);
  });

  it("refreshes Raju's orders page before redirecting", async () => {
    await placeOrder(START, form(GOOD)).catch(() => {});
    expect(revalidatePath.mock.calls, 'a new order should call revalidatePath("/orders") (before redirect, which throws)').toContainEqual(["/orders"]);
  });

  it("explains every mistake at once and saves nothing", async () => {
    const typed = { name: " ", phone: "98765", itemId: "DC-9999", qty: "6" };
    const result = await placeOrder(START, form(typed));
    expect(result?.errors, "all four fields are wrong, so all four messages should come back").toEqual({
      name: "Please enter your name.",
      phone: "Phone needs 10 digits, like 98765 43210.",
      itemId: "Please choose an item.",
      qty: "Choose between 1 and 5.",
    });
    expect(result?.values, "values should hold what was typed").toEqual(typed);
    expect(listOrders().length, "nothing should be saved when there is a mistake").toBe(0);
  });
});
