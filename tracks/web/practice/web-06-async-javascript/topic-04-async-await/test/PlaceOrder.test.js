import { describe, it, expect, vi } from "vitest";
import { placeOrder } from "../src/order.js";

/** A rejected promise that doesn't count as "unhandled" before your code awaits it. */
function failure(message) {
  const promise = Promise.reject(new Error(message));
  promise.catch(() => {});
  return promise;
}

/** A pretend shop API. log records what happened, in order. */
function fakeApi({ soldOut = false, declined = false } = {}) {
  const log = [];
  const api = {
    reserve: vi.fn((itemId) => {
      log.push(`reserve ${itemId}`);
      if (soldOut) return failure(`${itemId} sold out`);
      return Promise.resolve().then(() => {
        log.push(`reserved ${itemId}`);
      });
    }),
    pay: vi.fn((paise) => {
      log.push(`pay ${paise}`);
      if (declined) return failure("Bank declined the payment");
      return Promise.resolve({ ref: "UPI-5521" });
    }),
    release: vi.fn((itemId) => {
      log.push(`release ${itemId}`);
      return Promise.resolve();
    }),
  };
  return { api, log };
}

const golu = { id: "order-17", itemId: "DC-1042", paise: 45000 };

describe("placeOrder", () => {
  it("confirms the order with the real UPI reference", async () => {
    const { api } = fakeApi();
    const text = await placeOrder(api, golu);
    expect(text, "await api.pay so you read ref from the receipt, not from a promise").toBe("Order order-17 confirmed, UPI ref UPI-5521");
  });

  it("finishes reserving the item before it starts the payment", async () => {
    const { api, log } = fakeApi();
    await placeOrder(api, golu);
    expect(log, "the item should be reserved (and the reservation finished) before pay is called").toEqual([
      "reserve DC-1042",
      "reserved DC-1042",
      "pay 45000",
    ]);
  });

  it("says the item sold out and never takes money when reserving fails", async () => {
    const { api } = fakeApi({ soldOut: true });
    let text = null;
    let error = null;
    try {
      text = await placeOrder(api, golu);
    } catch (e) {
      error = e;
    }
    expect(error, "placeOrder should catch the sold-out error itself, not throw it").toBeNull();
    expect(text, "a failed reservation should give the sold-out message").toBe("Sorry, DC-1042 just sold out.");
    expect(api.pay.mock.calls.length, "pay must not be called when the item couldn't be reserved").toBe(0);
  });

  it("releases the item and says the payment failed when the bank declines", async () => {
    const { api } = fakeApi({ declined: true });
    let text = null;
    let error = null;
    try {
      text = await placeOrder(api, golu);
    } catch (e) {
      error = e;
    }
    expect(error, "placeOrder should catch the bank's error itself, not throw it").toBeNull();
    expect(text, "a declined payment should give the payment-failed message").toBe("Payment failed for order-17. Please try again.");
    expect(api.release.mock.calls, "the held item DC-1042 should be released once").toEqual([["DC-1042"]]);
  });

  it("uses the order it is given, not fixed values", async () => {
    const { api } = fakeApi();
    const text = await placeOrder(api, { id: "order-18", itemId: "DC-3001", paise: 89900 });
    expect(api.reserve.mock.calls[0], "reserve should get this order's itemId").toEqual(["DC-3001"]);
    expect(api.pay.mock.calls[0], "pay should get this order's paise").toEqual([89900]);
    expect(text, "the message should name this order").toBe("Order order-18 confirmed, UPI ref UPI-5521");
  });

  it("doesn't release anything when the order goes through", async () => {
    const { api } = fakeApi();
    await placeOrder(api, golu);
    expect(api.release.mock.calls.length, "release is only for failed payments").toBe(0);
  });
});
