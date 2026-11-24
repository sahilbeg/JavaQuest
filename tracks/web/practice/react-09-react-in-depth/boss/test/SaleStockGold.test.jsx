import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup, act, waitFor } from "@testing-library/react";
import { SaleStock } from "../src/SaleStock.jsx";
import { STOCK, reply, stubFetch, heldFetch, settle } from "./api.js";

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe("SaleStock (gold)", () => {
  it("says Checking stock straight after switching, never the old number under the new id", async () => {
    // DC-1042 answers at once; DC-2001 is stuck on slow 3G and never answers.
    stubFetch((url) => (url === "/api/stock/DC-1042" ? Promise.resolve(reply(STOCK[url])) : new Promise(() => {})));
    const { rerender } = render(<SaleStock itemId="DC-1042" />);
    await waitFor(() => expect(screen.queryByText("DC-1042: 7 left"), "DC-1042's stock should load first").not.toBeNull());
    rerender(<SaleStock itemId="DC-2001" />);
    expect(screen.queryByText("DC-2001: 7 left"), "DC-1042's 7 must not show under DC-2001's id").toBeNull();
    expect(
      screen.queryByText("Checking stock for DC-2001..."),
      "while DC-2001 loads, the badge should say Checking stock for DC-2001...",
    ).not.toBeNull();
  });

  it("ignores a slow answer for the item it switched away from", async () => {
    const { waiting } = heldFetch();
    const { rerender } = render(<SaleStock itemId="DC-1042" />);
    rerender(<SaleStock itemId="DC-2001" />);
    expect(typeof waiting["/api/stock/DC-2001"], "after switching, the badge should ask for DC-2001").toBe("function");

    await act(async () => {
      waiting["/api/stock/DC-2001"](reply(STOCK["/api/stock/DC-2001"]));
      await settle();
    });
    expect(screen.queryByText("DC-2001: 3 left"), "DC-2001's answer should be shown").not.toBeNull();

    await act(async () => {
      waiting["/api/stock/DC-1042"](reply(STOCK["/api/stock/DC-1042"]));
      await settle();
    });
    expect(screen.queryByText("DC-2001: 7 left"), "the late answer for DC-1042 must be ignored (use a flag set in the cleanup)").toBeNull();
    expect(screen.queryByText("DC-2001: 3 left"), "the badge should still show DC-2001: 3 left").not.toBeNull();
  });
});
