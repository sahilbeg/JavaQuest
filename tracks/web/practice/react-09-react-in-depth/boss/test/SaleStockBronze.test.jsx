import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup, act, waitFor } from "@testing-library/react";
import { SaleStock } from "../src/SaleStock.jsx";
import { stockApi, settle } from "./api.js";

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe("SaleStock (bronze)", () => {
  it("says it is checking before the answer arrives", () => {
    stockApi();
    render(<SaleStock itemId="DC-1042" />);
    expect(screen.queryByText("Checking stock for DC-1042..."), "before the answer, the badge should say Checking stock for DC-1042...").not.toBeNull();
  });

  it("sends exactly one request when the page opens, and shows the answer", async () => {
    const fetchMock = stockApi();
    render(<SaleStock itemId="DC-1042" />);
    await waitFor(() => expect(screen.queryByText("DC-1042: 7 left"), "the badge should show DC-1042: 7 left").not.toBeNull());
    await act(async () => {
      await settle();
    });
    expect(
      fetchMock.mock.calls.length,
      "one page view should send one request; this badge asks again after every render (look at the effect's dependency array)",
    ).toBe(1);
    expect(fetchMock.mock.calls[0][0], "the request should go to /api/stock/DC-1042").toBe("/api/stock/DC-1042");
  });
});
