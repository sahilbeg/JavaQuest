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
  // TODO 1: ask GET for id DC-9999. Expect status 404 and the JSON { error: "No item DC-9999" }.
}

export async function checkItemPage({ ItemPage }) {
  // TODO 2: render(await ItemPage({ params: Promise.resolve({ id: "DC-1042" }) })), then expect an h1 named "Golu the elephant" and the text "₹450.00".
}

export async function checkItemNotFound({ ItemPage }) {
  // TODO 3: call ItemPage with params for DC-9999 and expect the promise to reject with "NEXT_NOT_FOUND" (await expect(...).rejects.toThrow(...)).
}

export async function checkPlaceOrder({ placeOrder }) {
  // TODO 4: place an order with a FormData holding id "DC-1042" and qty "0", and expect { ok: false, error: "Pick a quantity from 1 to 5" }.
  // Then id "DC-1042" and qty "2": expect { ok: true, orderId: "order-1", totalPaise: 90000 }.
}
