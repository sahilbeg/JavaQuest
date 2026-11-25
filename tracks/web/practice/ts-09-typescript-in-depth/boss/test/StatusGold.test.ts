import { describe, it, expect } from "vitest";
import { statusLabel, canCancel } from "../src/orderStatus.ts";
import { lost } from "./statuses.ts";

describe("Gold: a status nobody handled is an error, not a wrong label", () => {
  it("throws for an unknown status instead of calling it a cancellation", () => {
    expect(() => statusLabel(lost), "LOST must not be shown as Cancelled: it should throw through assertNever").toThrow("Unknown order status: LOST");
  });

  it("throws for an unknown status instead of guessing whether it can be cancelled", () => {
    expect(() => canCancel(lost), "canCancel must not guess for LOST: it should throw through assertNever").toThrow("Unknown order status: LOST");
  });
});
