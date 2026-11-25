import { describe, it, expect } from "vitest";
import { statusLabel } from "../src/orderStatus.ts";
import { placed, packed, shipped, delivered, cancelled, returned } from "./statuses.ts";

describe("Bronze: every status gets the right label", () => {
  it("tells a customer their order was returned and the refund is coming", () => {
    expect(statusLabel(returned), "a returned order must say so, with the refund in rupees").toBe("Returned, refund of ₹450.00 on its way");
  });

  it("shows the reason for a cancelled order", () => {
    expect(statusLabel(cancelled), "a cancelled order shows its reason").toBe("Cancelled: changed my mind");
  });

  it("still labels the older statuses the same way", () => {
    expect(statusLabel(placed), "a new order says Order placed").toBe("Order placed");
    expect(statusLabel(packed), "a packed order says who packed it").toBe("Packed by Raju");
    expect(statusLabel(shipped), "a shipped order names the courier").toBe("On the way with Swift Couriers");
    expect(statusLabel(delivered), "a delivered order shows the day").toBe("Delivered on 14 Nov");
  });
});
