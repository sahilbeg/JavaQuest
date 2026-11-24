import { describe, it, expect, afterEach, vi } from "vitest";
import { deliveryUrl, checkDelivery, deliveryText } from "../src/api.js";
import { pretendApi } from "./server.js";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("deliveryUrl", () => {
  it("puts the PIN in the query string", () => {
    expect(deliveryUrl("411001"), "PIN 411001 should give /api/delivery?pin=411001").toBe("/api/delivery?pin=411001");
    expect(deliveryUrl("560001"), "the URL should use the PIN it is given").toBe("/api/delivery?pin=560001");
  });
});

describe("checkDelivery", () => {
  it("asks the delivery endpoint with the PIN", async () => {
    const fetchMock = pretendApi();
    await checkDelivery("411001");
    expect(fetchMock.mock.calls.length, "checkDelivery should call fetch once").toBe(1);
    expect(fetchMock.mock.calls[0]?.[0], "fetch should be called with /api/delivery?pin=411001").toBe("/api/delivery?pin=411001");
  });

  it("gives back the server's answer for a Pune PIN", async () => {
    pretendApi();
    const result = await checkDelivery("411001");
    expect(result, "a Pune PIN gets { deliverable: true, days: 2 } from the server").toEqual({ deliverable: true, days: 2 });
  });

  it("gives back the server's answer for a PIN outside Pune", async () => {
    pretendApi();
    const result = await checkDelivery("560001");
    expect(result, "a Bengaluru PIN gets { deliverable: false } from the server").toEqual({ deliverable: false });
  });

  it("throws an error that names the status when the PIN is refused", async () => {
    pretendApi();
    let error = null;
    try {
      await checkDelivery("41100");
    } catch (e) {
      error = e;
    }
    expect(error, "a 5-digit PIN gets a 400 answer, so checkDelivery should throw").not.toBeNull();
    expect(String(error?.message), "the error message should include the status 400").toContain("400");
  });
});

describe("deliveryText", () => {
  it("says how many days delivery takes when the PIN is deliverable", () => {
    expect(deliveryText("411001", { deliverable: true, days: 2 }), "deliverable in 2 days").toBe("Delivery to 411001 in 2 days");
    expect(deliveryText("411038", { deliverable: true, days: 3 }), "use the pin and days you are given").toBe("Delivery to 411038 in 3 days");
  });

  it("apologises when the PIN is not deliverable", () => {
    expect(deliveryText("560001", { deliverable: false }), "not deliverable").toBe("Sorry, we don't deliver to 560001 yet");
  });
});
