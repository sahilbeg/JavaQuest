import { describe, it, expect, afterEach, vi } from "vitest";
import { renderHook, waitFor, act, cleanup } from "@testing-library/react";
import { useFetch } from "../src/useFetch.js";

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

/** A pretend server. Each fetch waits until the test calls reply(...) or fail(...). An aborted fetch rejects with AbortError, like the real one. */
function fakeServer() {
  const calls = [];
  vi.stubGlobal(
    "fetch",
    vi.fn(
      (url, options = {}) =>
        new Promise((resolve, reject) => {
          const signal = options.signal;
          calls.push({
            url,
            signal,
            reply: (body, status = 200) => resolve({ ok: status >= 200 && status < 300, status, json: async () => body }),
            fail: (error) => reject(error),
          });
          signal?.addEventListener("abort", () => reject(new DOMException("The operation was aborted.", "AbortError")));
        })
    )
  );
  return calls;
}

/** Lets every pending promise settle. */
async function settle() {
  await act(() => new Promise((resolve) => setTimeout(resolve, 0)));
}

describe("Round 3: useFetch", () => {
  it("starts loading, then gives the data", async () => {
    const calls = fakeServer();
    const { result } = renderHook(() => useFetch("/api/stock/DC-1042"));
    expect(result.current.status, "before the answer arrives, status should be \"loading\"").toBe("loading");
    expect(calls.length, "useFetch should call fetch once").toBe(1);
    calls[0].reply({ id: "DC-1042", stock: 7 });
    await waitFor(() => expect(result.current.status, "after the answer, status should be \"success\"").toBe("success"));
    expect(result.current.data, "data should be the JSON the server sent").toEqual({ id: "DC-1042", stock: 7 });
    expect(result.current.error, "a success has no error").toBe(null);
  });

  it("turns an answer that isn't ok into an error", async () => {
    const calls = fakeServer();
    const { result } = renderHook(() => useFetch("/api/stock/DC-1042"));
    calls[0].reply({ message: "oops" }, 500);
    await waitFor(() => expect(result.current.status, "a 500 answer should give status \"error\", not success: check response.ok (TODO 6)").toBe("error"));
    expect(result.current.error, "the error should say \"HTTP 500\"").toBe("HTTP 500");
    expect(result.current.data, "an error has no data").toBe(null);
  });

  it("turns a network failure into an error", async () => {
    const calls = fakeServer();
    const { result } = renderHook(() => useFetch("/api/stock/DC-1042"));
    calls[0].fail(new TypeError("Failed to fetch"));
    await waitFor(() => expect(result.current.status, "a failed fetch should give status \"error\", not a spinner forever (TODO 6)").toBe("error"));
    expect(result.current.error, "the error should be the failure's message").toBe("Failed to fetch");
  });

  it("goes back to loading when the url changes", async () => {
    const calls = fakeServer();
    const { result, rerender } = renderHook(({ url }) => useFetch(url), { initialProps: { url: "/api/stock/DC-1042" } });
    calls[0].reply({ id: "DC-1042", stock: 7 });
    await waitFor(() => expect(result.current.status, "the first answer should arrive").toBe("success"));
    rerender({ url: "/api/stock/DC-1301" });
    expect(result.current.status, "a new url should set status back to \"loading\" (TODO 5)").toBe("loading");
    expect(result.current.data, "while the new url loads, the old product's data must not show (TODO 5)").toBe(null);
  });

  it("aborts the old request when the url changes, and shows only the new answer", async () => {
    const calls = fakeServer();
    const { result, rerender } = renderHook(({ url }) => useFetch(url), { initialProps: { url: "/api/stock/DC-1042" } });
    expect(calls[0].signal?.aborted, "fetch should get a signal from an AbortController: fetch(url, { signal: controller.signal }) (TODO 7)").toBe(false);
    rerender({ url: "/api/stock/DC-1301" });
    expect(calls.length, "a new url should start a second fetch").toBe(2);
    expect(calls[0].signal.aborted, "changing the url should abort the old request in the effect's cleanup (TODO 7)").toBe(true);
    await settle();
    expect(result.current.status, "an aborted request is not an error: ignore the AbortError and keep loading (TODO 7)").toBe("loading");
    calls[1].reply({ id: "DC-1301", stock: 2 });
    await waitFor(() => expect(result.current.status, "the new product's answer should arrive").toBe("success"));
    expect(result.current.data, "only the tote bag's stock should show, never Golu's").toEqual({ id: "DC-1301", stock: 2 });
  });

  it("aborts the request when the component goes away", () => {
    const calls = fakeServer();
    const { unmount } = renderHook(() => useFetch("/api/stock/DC-1042"));
    unmount();
    expect(calls[0].signal?.aborted, "unmounting should abort the request, so nothing updates a component that's gone (TODO 7)").toBe(true);
  });
});
