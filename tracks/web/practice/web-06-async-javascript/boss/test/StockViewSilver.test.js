import { describe, it, expect, afterEach, vi } from "vitest";
import { createStockView } from "../src/stockView.js";
import { slowServer, settle, freshPanel } from "./stockServer.js";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("Silver: cancel the old request", () => {
  it("gives every request an abort signal", async () => {
    const server = slowServer();
    const show = createStockView(freshPanel());
    const done = show("DC-1042");
    expect(server.fetchMock.mock.calls[0]?.[1]?.signal, "pass { signal: controller.signal } as fetch's second argument").toBeTruthy();
    server.answer("DC-1042");
    await settle(done);
  });

  it("aborts the old request when the customer taps another item", async () => {
    const server = slowServer();
    const show = createStockView(freshPanel());
    const golu = show("DC-1042");
    const coasters = show("DC-2001");
    const oldSignal = server.fetchMock.mock.calls[0]?.[1]?.signal;
    const newSignal = server.fetchMock.mock.calls[1]?.[1]?.signal;
    expect(oldSignal?.aborted, "starting the coaster set's request should abort Golu's").toBe(true);
    expect(newSignal?.aborted, "the new request needs a fresh AbortController, so its signal isn't aborted").toBe(false);
    server.answer("DC-2001");
    server.answer("DC-1042");
    await settle(coasters);
    await settle(golu);
  });

  it("handles the aborted request quietly", async () => {
    const server = slowServer({ honourAbort: true });
    const panel = freshPanel();
    const show = createStockView(panel);
    const golu = show("DC-1042");
    const coasters = show("DC-2001");
    server.answer("DC-2001");
    const coastersError = await settle(coasters);
    server.answer("DC-1042"); // too late for an aborted request; it only matters if the request was never aborted
    const goluError = await settle(golu);
    expect(goluError, "the aborted request's AbortError should be caught inside show, not thrown").toBeNull();
    expect(coastersError, "the newest request should finish without an error").toBeNull();
    expect(panel.textContent, "the panel should show the coaster set's stock").toBe("12 in stock");
    expect(panel.dataset.state, "an abort is not a failure, so the panel should end up \"ready\"").toBe("ready");
  });

  it("can still check the same panel again after a request finished", async () => {
    const server = slowServer({ honourAbort: true });
    const panel = freshPanel();
    const show = createStockView(panel);
    const first = show("DC-2001");
    server.answer("DC-2001");
    await settle(first);
    const second = show("DC-3001");
    expect(server.fetchMock.mock.calls[1]?.[1]?.signal?.aborted, "each request needs its own new controller").toBe(false);
    server.answer("DC-3001");
    const error = await settle(second);
    expect(error, "the second check should work").toBeNull();
    expect(panel.textContent, "the panel should show the tote bag's stock").toBe("3 in stock");
  });
});
