import { describe, it, expect, afterEach, vi } from "vitest";
import { createStockView } from "../src/stockView.js";
import { slowServer, settle, freshPanel } from "./stockServer.js";

const LOADING = "Checking stock...";
const FAILED = "Couldn't check stock. Try again.";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("Gold: loading and error states that never lie", () => {
  it("shows the loading message the moment an item is tapped", async () => {
    const server = slowServer();
    const panel = freshPanel();
    const show = createStockView(panel);
    const done = show("DC-2001");
    expect(panel.textContent, "set LOADING before the first await").toBe(LOADING);
    expect(panel.dataset.state, "data-state should be \"loading\" while waiting").toBe("loading");
    server.answer("DC-2001");
    await settle(done);
    expect(panel.dataset.state, "after the answer, data-state should be \"ready\"").toBe("ready");
  });

  it("goes back to loading when the customer taps the next item", async () => {
    const server = slowServer();
    const panel = freshPanel();
    const show = createStockView(panel);
    const first = show("DC-2001");
    server.answer("DC-2001");
    await settle(first);
    const second = show("DC-3001");
    expect(panel.textContent, "the old item's stock must not stay up while the new one loads").toBe(LOADING);
    server.answer("DC-3001");
    await settle(second);
    expect(panel.textContent, "the tote bag has 3").toBe("3 in stock");
  });

  it("shows the error message when the server breaks", async () => {
    const server = slowServer();
    const panel = freshPanel();
    const show = createStockView(panel);
    const done = show("DC-2001");
    server.breakDown("DC-2001");
    const error = await settle(done);
    expect(error, "show should catch the failure itself, not throw").toBeNull();
    expect(panel.textContent, "a 500 answer should show the friendly error message").toBe(FAILED);
    expect(panel.dataset.state, "a 500 answer should set data-state to \"error\"").toBe("error");
  });

  it("shows the error message when the connection drops", async () => {
    const server = slowServer();
    const panel = freshPanel();
    const show = createStockView(panel);
    const done = show("DC-2001");
    server.dropConnection("DC-2001");
    const error = await settle(done);
    expect(error, "show should catch the network error itself, not throw").toBeNull();
    expect(panel.textContent, "a dropped connection should show the friendly error message").toBe(FAILED);
    expect(panel.dataset.state, "a dropped connection should set data-state to \"error\"").toBe("error");
  });

  it("never lets an old request's failure overwrite the newest answer", async () => {
    const server = slowServer();
    const panel = freshPanel();
    const show = createStockView(panel);
    const golu = show("DC-1042");
    const coasters = show("DC-2001");
    server.answer("DC-2001");
    await settle(coasters);
    server.dropConnection("DC-1042");
    const error = await settle(golu);
    expect(error, "the old request's failure should be caught inside show, not thrown").toBeNull();
    expect(panel.textContent, "the coaster set's \"12 in stock\" must stay; Golu's failure is out of date").toBe("12 in stock");
    expect(panel.dataset.state, "the panel should still be \"ready\"").toBe("ready");
  });

  it("doesn't flash an error for an old request while the newest is still loading", async () => {
    const server = slowServer();
    const panel = freshPanel();
    const show = createStockView(panel);
    const golu = show("DC-1042");
    const coasters = show("DC-2001");
    server.dropConnection("DC-1042");
    await settle(golu);
    expect(panel.dataset.state, "the coaster set is still loading, so the old failure must not set \"error\"").toBe("loading");
    expect(panel.textContent, "the loading message should stay up").toBe(LOADING);
    server.answer("DC-2001");
    await settle(coasters);
    expect(panel.textContent, "then the coaster set's answer arrives").toBe("12 in stock");
  });
});
