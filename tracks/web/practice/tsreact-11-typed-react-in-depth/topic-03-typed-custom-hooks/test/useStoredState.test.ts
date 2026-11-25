import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { renderHook, act, cleanup } from "@testing-library/react";
import { useStoredState } from "../src/useStoredState.ts";

beforeEach(() => {
  localStorage.clear();
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

function isIdList(value: unknown): value is string[] {
  return Array.isArray(value) && value.every((v) => typeof v === "string");
}

describe("useStoredState", () => {
  it("starts from initial when nothing is saved", () => {
    const { result } = renderHook(() => useStoredState<string[]>("dhaaga-wishlist", []));
    expect(result.current[0], "with nothing saved, the value should be the initial []").toEqual([]);
  });

  it("starts from the value saved in localStorage", () => {
    localStorage.setItem("dhaaga-wishlist", '["DC-1042"]');
    const { result } = renderHook(() => useStoredState<string[]>("dhaaga-wishlist", []));
    expect(result.current[0], "the saved JSON [\"DC-1042\"] should be the starting value").toEqual(["DC-1042"]);
  });

  it("saves a new value as JSON and shows it", () => {
    const { result } = renderHook(() => useStoredState<string[]>("dhaaga-wishlist", []));
    act(() => result.current[1](["DC-2001"]));
    expect(result.current[0], "after save([\"DC-2001\"]) the value should be [\"DC-2001\"]").toEqual(["DC-2001"]);
    expect(localStorage.getItem("dhaaga-wishlist"), "save should write JSON.stringify(next) under the key").toBe('["DC-2001"]');
  });

  it("remembers the value after the component is mounted again", () => {
    const first = renderHook(() => useStoredState<number>("dhaaga-qty", 1));
    act(() => first.result.current[1](3));
    first.unmount();
    const second = renderHook(() => useStoredState<number>("dhaaga-qty", 1));
    expect(second.result.current[0], "a fresh mount should read back the saved 3, like after a refresh").toBe(3);
  });

  it("keeps different keys apart", () => {
    localStorage.setItem("dhaaga-colour", '"Mint"');
    const { result } = renderHook(() => useStoredState<string>("dhaaga-size", "small"));
    expect(result.current[0], "nothing is saved under dhaaga-size, so it should start at small").toBe("small");
  });

  it("falls back to initial when the saved text is broken", () => {
    localStorage.setItem("dhaaga-wishlist", "[oops");
    const { result } = renderHook(() => useStoredState<string[]>("dhaaga-wishlist", []));
    expect(result.current[0], "broken JSON should not crash: use the initial [] instead (try/catch around JSON.parse)").toEqual([]);
  });

  it("falls back to initial when the guard says the shape is wrong", () => {
    localStorage.setItem("dhaaga-wishlist", '"DC-1042"');
    const { result } = renderHook(() => useStoredState<string[]>("dhaaga-wishlist", [], isIdList));
    expect(result.current[0], "a saved string is not a list of ids, so isValid says no and the value should be []").toEqual([]);
  });

  it("trusts a saved value the guard accepts", () => {
    localStorage.setItem("dhaaga-wishlist", '["DC-1042","DC-3005"]');
    const { result } = renderHook(() => useStoredState<string[]>("dhaaga-wishlist", [], isIdList));
    expect(result.current[0], "a saved list of ids passes the guard and should be used").toEqual(["DC-1042", "DC-3005"]);
  });

  it("reads localStorage only once, not on every render", () => {
    localStorage.setItem("dhaaga-wishlist", '["DC-1042"]');
    const getItem = vi.spyOn(Storage.prototype, "getItem");
    const { rerender } = renderHook(() => useStoredState<string[]>("dhaaga-wishlist", []));
    rerender();
    rerender();
    const reads = getItem.mock.calls.filter(([key]) => key === "dhaaga-wishlist");
    expect(reads.length, "three renders should read storage once: pass a function to useState (a lazy initializer)").toBe(1);
  });
});
