import { describe, it, expect } from "vitest";
import { canCancel } from "../src/orderStatus.ts";
import { placed, packed, shipped, delivered, cancelled, returned } from "./statuses.ts";

describe("Silver: only orders still in the shop can be cancelled", () => {
  it("lets customers cancel a placed or packed order", () => {
    expect(canCancel(placed), "a placed order hasn't left the shop, so it can be cancelled").toBe(true);
    expect(canCancel(packed), "a packed order hasn't left the shop, so it can be cancelled").toBe(true);
  });

  it("doesn't let customers cancel an order that has left", () => {
    expect(canCancel(shipped), "a shipped order is with the courier: too late to cancel").toBe(false);
    expect(canCancel(delivered), "a delivered order can't be cancelled").toBe(false);
  });

  it("doesn't show Cancel on an order that is already finished", () => {
    expect(canCancel(cancelled), "a cancelled order can't be cancelled again").toBe(false);
    expect(canCancel(returned), "a returned order can't be cancelled").toBe(false);
  });
});
