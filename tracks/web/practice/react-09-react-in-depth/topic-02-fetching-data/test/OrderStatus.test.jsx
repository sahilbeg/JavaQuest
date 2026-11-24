import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup, act, waitFor } from "@testing-library/react";
import { OrderStatus } from "../src/OrderStatus.jsx";
import { reply, stubFetch, heldFetch, settle } from "./api.js";

const ORDERS = {
  "/api/orders/order-17": { id: "order-17", status: "PACKED" },
  "/api/orders/order-18": { id: "order-18", status: "SHIPPED" },
};

/** A pretend API that knows order-17 and order-18, and answers 404 for anything else. */
function fakeApi() {
  return stubFetch(async (url) => (ORDERS[url] ? reply(ORDERS[url]) : reply({ message: "not found" }, 404)));
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe("OrderStatus", () => {
  it("shows a loading message while it waits for the server", () => {
    stubFetch(() => new Promise(() => {}));
    render(<OrderStatus orderId="order-17" />);
    expect(screen.queryByText("Loading order-17..."), "before the answer arrives the page should say Loading order-17...").not.toBeNull();
  });

  it("asks the API for the order, once", async () => {
    const fetchMock = fakeApi();
    render(<OrderStatus orderId="order-17" />);
    await waitFor(() => expect(fetchMock.mock.calls.length, "the component should call fetch once after it appears").toBe(1));
    expect(fetchMock.mock.calls[0][0], "the request should go to /api/orders/order-17").toBe("/api/orders/order-17");
  });

  it("shows the order's status when the answer arrives", async () => {
    fakeApi();
    render(<OrderStatus orderId="order-17" />);
    await waitFor(() => expect(screen.queryByText("Status: PACKED"), "order-17 is PACKED, so the page should say Status: PACKED").not.toBeNull());
    expect(screen.queryByRole("heading", { name: "order-17" }), "the order id should be the heading").not.toBeNull();
  });

  it("shows an error when the server says the order doesn't exist", async () => {
    fakeApi();
    render(<OrderStatus orderId="order-99" />);
    await waitFor(() =>
      expect(screen.queryByText("We couldn't load order-99."), "a 404 answer should show We couldn't load order-99. (check res.ok)").not.toBeNull(),
    );
  });

  it("shows an error when the request fails completely", async () => {
    stubFetch(async () => {
      throw new TypeError("Failed to fetch");
    });
    render(<OrderStatus orderId="order-17" />);
    await waitFor(() =>
      expect(screen.queryByText("We couldn't load order-17."), "when fetch throws, the page should show We couldn't load order-17.").not.toBeNull(),
    );
  });

  it("loads the new order when orderId changes", async () => {
    const fetchMock = fakeApi();
    const { rerender } = render(<OrderStatus orderId="order-17" />);
    await waitFor(() => expect(screen.queryByText("Status: PACKED"), "order-17 should load first").not.toBeNull());
    rerender(<OrderStatus orderId="order-18" />);
    await waitFor(() =>
      expect(screen.queryByText("Status: SHIPPED"), "after switching to order-18 the page should show its status, SHIPPED").not.toBeNull(),
    );
    expect(fetchMock.mock.calls.length, "one request for order-17 and one for order-18").toBe(2);
  });

  it("shows the loading message again while the new order loads", async () => {
    const { waiting } = heldFetch();
    const { rerender } = render(<OrderStatus orderId="order-17" />);
    expect(typeof waiting["/api/orders/order-17"], "the component should ask for order-17").toBe("function");
    await act(async () => {
      waiting["/api/orders/order-17"](reply(ORDERS["/api/orders/order-17"]));
      await settle();
    });
    rerender(<OrderStatus orderId="order-18" />);
    expect(screen.queryByText("Status: PACKED"), "order-17's status should not stay on the page under order-18").toBeNull();
    expect(screen.queryByText("Loading order-18..."), "while order-18 loads the page should say Loading order-18...").not.toBeNull();
  });

  it("ignores a slow answer for an order it no longer shows", async () => {
    const { waiting } = heldFetch();
    const { rerender } = render(<OrderStatus orderId="order-17" />);
    rerender(<OrderStatus orderId="order-18" />);
    expect(typeof waiting["/api/orders/order-18"], "after switching, the component should ask for order-18").toBe("function");

    await act(async () => {
      waiting["/api/orders/order-18"](reply(ORDERS["/api/orders/order-18"]));
      await settle();
    });
    expect(screen.queryByText("Status: SHIPPED"), "order-18's answer should be shown").not.toBeNull();

    await act(async () => {
      waiting["/api/orders/order-17"](reply(ORDERS["/api/orders/order-17"]));
      await settle();
    });
    expect(screen.queryByText("Status: PACKED"), "the late answer for order-17 should be ignored (use an ignore flag set in the cleanup)").toBeNull();
    expect(screen.queryByText("Status: SHIPPED"), "the page should still show order-18").not.toBeNull();
  });
});
