import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup, act, waitFor } from "@testing-library/react";
import { SaleStock } from "../src/SaleStock.jsx";
import { stockApi, settle } from "./api.js";

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe("SaleStock (silver)", () => {
  it("sends no request when the page renders again for the same item", async () => {
    const fetchMock = stockApi();
    const { rerender } = render(<SaleStock itemId="DC-1042" />);
    await waitFor(() => expect(screen.queryByText("DC-1042: 7 left"), "DC-1042's stock should load first").not.toBeNull());
    rerender(<SaleStock itemId="DC-1042" />);
    await act(async () => {
      await settle();
    });
    expect(fetchMock.mock.calls.length, "rendering again for the same item should not ask the API again").toBe(1);
  });

  it("sends one new request when the item changes, and shows the new stock", async () => {
    const fetchMock = stockApi();
    const { rerender } = render(<SaleStock itemId="DC-1042" />);
    await waitFor(() => expect(screen.queryByText("DC-1042: 7 left"), "DC-1042's stock should load first").not.toBeNull());
    rerender(<SaleStock itemId="DC-2001" />);
    await waitFor(() =>
      expect(screen.queryByText("DC-2001: 3 left"), "after switching to DC-2001 the badge should show DC-2001: 3 left").not.toBeNull(),
    );
    await act(async () => {
      await settle();
    });
    expect(
      fetchMock.mock.calls.map((call) => call[0]),
      "there should be exactly one request for DC-1042, then exactly one for DC-2001",
    ).toEqual(["/api/stock/DC-1042", "/api/stock/DC-2001"]);
  });
});
