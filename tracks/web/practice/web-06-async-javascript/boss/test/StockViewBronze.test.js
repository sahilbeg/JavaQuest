import { describe, it, expect, afterEach, vi } from "vitest";
import { createStockView } from "../src/stockView.js";
import { slowServer, settle, freshPanel } from "./stockServer.js";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("Bronze: only the newest request may change the panel", () => {
  it("shows the stock for one item", async () => {
    const server = slowServer();
    const panel = freshPanel();
    const show = createStockView(panel);
    const done = show("DC-2001");
    server.answer("DC-2001");
    await settle(done);
    expect(panel.textContent, "the coaster set has 12, so the panel should say \"12 in stock\"").toBe("12 in stock");
  });

  it("asks the stock endpoint for the item", async () => {
    const server = slowServer();
    const show = createStockView(freshPanel());
    const done = show("DC-2001");
    server.answer("DC-2001");
    await settle(done);
    expect(server.fetchMock.mock.calls[0]?.[0], "fetch should be called with /api/stock/DC-2001").toBe("/api/stock/DC-2001");
  });

  it("doesn't let a slow answer for an older tap overwrite the newer one", async () => {
    const server = slowServer();
    const panel = freshPanel();
    const show = createStockView(panel);
    const golu = show("DC-1042");
    const coasters = show("DC-2001");
    server.answer("DC-2001");
    await settle(coasters);
    server.answer("DC-1042");
    await settle(golu);
    expect(panel.textContent, "the customer is looking at the coaster set; Golu's late \"Sold out\" must be ignored").toBe("12 in stock");
  });

  it("ignores an older tap's answer even when it arrives first", async () => {
    const server = slowServer();
    const panel = freshPanel();
    const show = createStockView(panel);
    const golu = show("DC-1042");
    const coasters = show("DC-2001");
    server.answer("DC-1042");
    await settle(golu);
    expect(panel.textContent, "Golu's answer is already out of date, because the coaster set was tapped after it").not.toBe("Sold out");
    server.answer("DC-2001");
    await settle(coasters);
    expect(panel.textContent, "the coaster set's own answer should end up on the panel").toBe("12 in stock");
  });

  it("keeps a separate count for each panel", async () => {
    const server = slowServer();
    const first = freshPanel();
    const second = document.createElement("p");
    document.body.append(second);
    const showFirst = createStockView(first);
    const showSecond = createStockView(second);
    const a = showFirst("DC-2001");
    const b = showSecond("DC-3001");
    server.answer("DC-2001");
    server.answer("DC-3001");
    await settle(a);
    await settle(b);
    expect(first.textContent, "each panel's only request is its newest, so the first panel should show its answer").toBe("12 in stock");
    expect(second.textContent, "the second panel should show its own answer too").toBe("3 in stock");
  });
});
