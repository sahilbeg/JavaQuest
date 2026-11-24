import { describe, it, expect, afterEach, vi } from "vitest";
import { createOrder } from "../src/api.js";
import { pretendApi } from "./server.js";

const order = { itemId: "DC-1042", qty: 1, pin: "411038", payment: "UPI" };

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("createOrder", () => {
  it("gives back the id of the new order", async () => {
    pretendApi();
    const id = await createOrder(order);
    expect(id, "the server answers 201 with { id: \"order-17\" }, so createOrder should return \"order-17\"").toBe("order-17");
  });

  it("sends a POST to /api/orders", async () => {
    const fetchMock = pretendApi();
    await createOrder(order).catch(() => {});
    const [url, options] = fetchMock.mock.calls[0] ?? [];
    expect(url, "fetch should be called with /api/orders").toBe("/api/orders");
    expect(options?.method, "the options should say method: \"POST\"").toBe("POST");
  });

  it("labels the body as JSON with a Content-Type header", async () => {
    const fetchMock = pretendApi();
    await createOrder(order).catch(() => {});
    const headers = fetchMock.mock.calls[0]?.[1]?.headers ?? {};
    const contentType = headers["Content-Type"] ?? headers["content-type"];
    expect(contentType, "headers should include \"Content-Type\": \"application/json\"").toBe("application/json");
  });

  it("sends the order as JSON text in the body", async () => {
    const fetchMock = pretendApi();
    await createOrder(order).catch(() => {});
    const body = fetchMock.mock.calls[0]?.[1]?.body;
    expect(typeof body, "the body should be text: use JSON.stringify(order)").toBe("string");
    expect(JSON.parse(body ?? "null"), "the body should be the order, as JSON").toEqual(order);
  });

  it("throws the server's own message when the order is refused", async () => {
    pretendApi();
    let error = null;
    try {
      await createOrder({ ...order, pin: "41103" });
    } catch (e) {
      error = e;
    }
    expect(error, "a 400 answer isn't ok, so createOrder should throw").not.toBeNull();
    expect(error?.message, "the error should carry the server's text from data.error").toBe("PIN must be 6 digits");
  });
});
