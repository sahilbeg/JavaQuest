import { describe, it, expect, beforeEach, vi } from "vitest";

vi.mock("next/cache", () => ({ revalidatePath: vi.fn(), revalidateTag: vi.fn(), updateTag: vi.fn() }));

import { revalidatePath, revalidateTag } from "next/cache";
import { afterPriceChange, afterHoursChange } from "../src/lib/refresh.js";

beforeEach(() => {
  vi.clearAllMocks();
});

describe("afterPriceChange", () => {
  it("marks the catalogue stale on every page that uses it", () => {
    afterPriceChange("DC-3007");
    expect(revalidateTag.mock.calls, 'a price change should call revalidateTag("catalogue", "max")').toContainEqual(["catalogue", "max"]);
  });

  it("refreshes the changed item's own page", () => {
    afterPriceChange("DC-3007");
    expect(revalidatePath.mock.calls, 'changing DC-3007 should call revalidatePath("/items/DC-3007")').toContainEqual(["/items/DC-3007"]);
  });

  it("uses the item id it was given", () => {
    afterPriceChange("DC-1042");
    expect(revalidatePath.mock.calls, 'changing DC-1042 should call revalidatePath("/items/DC-1042")').toContainEqual(["/items/DC-1042"]);
  });
});

describe("afterHoursChange", () => {
  it("marks the hours stale and leaves the catalogue alone", () => {
    afterHoursChange();
    expect(revalidateTag.mock.calls, 'an hours change should call revalidateTag("hours", "max")').toContainEqual(["hours", "max"]);
    expect(revalidateTag.mock.calls, "an hours change shouldn't clear the catalogue").not.toContainEqual(["catalogue", "max"]);
  });
});
