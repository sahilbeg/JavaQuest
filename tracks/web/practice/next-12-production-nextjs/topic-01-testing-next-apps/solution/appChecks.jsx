import { render, screen } from "@testing-library/react";
import { expect } from "vitest";

// Each check gets the app's parts in one object, { GET, ItemPage, placeOrder }, and takes the part it needs.
// Every check starts with an empty order list, so the first order placed is order-1.
// While the checks run, notFound() from next/navigation throws an Error with the message "NEXT_NOT_FOUND".

// Worked example: call the route handler with a real Request, then read the real Response.
export async function checkStockFound({ GET }) {
  const response = await GET(new Request("http://localhost/api/stock?id=DC-1042"));
  expect(response.status, "a known item should answer 200 OK").toBe(200);
  expect(await response.json(), "Golu has 7 in stock, sent as a number").toEqual({ id: "DC-1042", stock: 7 });
}

export async function checkStockUnknown({ GET }) {
  const response = await GET(new Request("http://localhost/api/stock?id=DC-9999"));
  expect(response.status, "an unknown item should answer 404 Not Found").toBe(404);
  expect(await response.json(), "the 404 should say which id is missing").toEqual({ error: "No item DC-9999" });
}

export async function checkItemPage({ ItemPage }) {
  render(await ItemPage({ params: Promise.resolve({ id: "DC-1042" }) }));
  expect(screen.queryByRole("heading", { level: 1, name: "Golu the elephant" }), "the page's h1 should be the item's name").not.toBeNull();
  expect(screen.queryByText("₹450.00"), "Golu costs 45000 paise, so the page should show ₹450.00").not.toBeNull();
}

export async function checkItemNotFound({ ItemPage }) {
  await expect(ItemPage({ params: Promise.resolve({ id: "DC-9999" }) }), "an unknown id should call notFound()").rejects.toThrow("NEXT_NOT_FOUND");
}

export async function checkPlaceOrder({ placeOrder }) {
  const zero = new FormData();
  zero.set("id", "DC-1042");
  zero.set("qty", "0");
  expect(await placeOrder(zero), "a quantity of 0 should be refused").toEqual({ ok: false, error: "Pick a quantity from 1 to 5" });

  const two = new FormData();
  two.set("id", "DC-1042");
  two.set("qty", "2");
  expect(await placeOrder(two), "two Golus should make order-1 for 90000 paise").toEqual({ ok: true, orderId: "order-1", totalPaise: 90000 });
}
