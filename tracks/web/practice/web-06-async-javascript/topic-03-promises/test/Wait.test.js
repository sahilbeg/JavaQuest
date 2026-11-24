import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { wait } from "../src/payment.js";

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
});

describe("wait", () => {
  it("gives back a promise", () => {
    const result = wait(1000);
    expect(typeof result?.then, "wait should return a promise (an object with a then method)").toBe("function");
  });

  it("is fulfilled only after the time it was given", async () => {
    let done = false;
    wait(1000).then(() => {
      done = true;
    });
    await vi.advanceTimersByTimeAsync(999);
    expect(done, "wait(1000) should still be pending after 999 ms; use setTimeout inside new Promise").toBe(false);
    await vi.advanceTimersByTimeAsync(1);
    expect(done, "wait(1000) should be fulfilled after 1000 ms").toBe(true);
  });

  it("uses its ms argument instead of a fixed time", async () => {
    let done = false;
    wait(250).then(() => {
      done = true;
    });
    await vi.advanceTimersByTimeAsync(249);
    expect(done, "wait(250) should still be pending after 249 ms").toBe(false);
    await vi.advanceTimersByTimeAsync(1);
    expect(done, "wait(250) should be fulfilled after 250 ms").toBe(true);
  });
});
