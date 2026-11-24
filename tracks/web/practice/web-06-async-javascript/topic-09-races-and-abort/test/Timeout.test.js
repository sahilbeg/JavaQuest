import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { fetchWithTimeout } from "../src/delivery.js";

/** A pretend server on a dead connection: it never answers, but it stops (with an AbortError) when its signal aborts. */
function deadConnection() {
  const fetchMock = vi.fn(
    (url, options = {}) =>
      new Promise((resolve, reject) => {
        options.signal?.addEventListener("abort", () => {
          const error = new Error("This operation was aborted");
          error.name = "AbortError";
          reject(error);
        });
      }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** A pretend server that answers after delay ms. */
function answersAfter(delay) {
  const fetchMock = vi.fn(
    () =>
      new Promise((resolve) => {
        setTimeout(() => resolve({ ok: true, status: 200, json: async () => ({ deliverable: true, days: 2 }) }), delay);
      }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** Watches a promise, so a test can check whether it has finished yet. */
function track(promise) {
  const state = { done: false, value: undefined, error: null };
  Promise.resolve(promise).then(
    (value) => {
      state.done = true;
      state.value = value;
    },
    (error) => {
      state.done = true;
      state.error = error;
    },
  );
  return state;
}

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe("fetchWithTimeout", () => {
  it("passes an abort signal to fetch", async () => {
    const fetchMock = deadConnection();
    track(fetchWithTimeout("/api/delivery?pin=411045", 5000));
    await vi.advanceTimersByTimeAsync(0);
    expect(fetchMock.mock.calls[0]?.[0], "fetch should get the same URL").toBe("/api/delivery?pin=411045");
    const signal = fetchMock.mock.calls[0]?.[1]?.signal;
    expect(signal, "pass { signal: controller.signal } as fetch's second argument").toBeTruthy();
    expect(signal?.aborted, "the signal should not be aborted before the time is up").toBe(false);
  });

  it("gives up with an AbortError when the time runs out", async () => {
    const fetchMock = deadConnection();
    const result = track(fetchWithTimeout("/api/delivery?pin=411045", 5000));
    await vi.advanceTimersByTimeAsync(4999);
    expect(result.done, "after 4.999 seconds, the request should still be waiting").toBe(false);
    await vi.advanceTimersByTimeAsync(1);
    expect(fetchMock.mock.calls[0]?.[1]?.signal?.aborted, "after 5 seconds, the timer should call controller.abort()").toBe(true);
    expect(result.error?.name, "the promise should reject with the AbortError from fetch").toBe("AbortError");
  });

  it("gives back the response when it arrives in time", async () => {
    answersAfter(1000);
    const result = track(fetchWithTimeout("/api/delivery?pin=411045", 5000));
    await vi.advanceTimersByTimeAsync(1000);
    expect(result.error, "an answer after 1 second is in time, so nothing should fail").toBeNull();
    expect(result.value?.ok, "fetchWithTimeout should resolve with fetch's response").toBe(true);
  });

  it("clears its timer once the answer arrives", async () => {
    const fetchMock = answersAfter(1000);
    track(fetchWithTimeout("/api/delivery?pin=411045", 5000));
    await vi.advanceTimersByTimeAsync(1000);
    expect(vi.getTimerCount(), "the 5-second timer should be cleared with clearTimeout once the answer is in").toBe(0);
    await vi.advanceTimersByTimeAsync(5000);
    expect(fetchMock.mock.calls[0]?.[1]?.signal?.aborted, "a request that already answered should never be aborted").toBe(false);
  });
});
