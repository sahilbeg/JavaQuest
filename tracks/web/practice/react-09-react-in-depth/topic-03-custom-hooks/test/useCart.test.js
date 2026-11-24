import { describe, it, expect, afterEach, beforeEach } from "vitest";
import { renderHook, act, cleanup } from "@testing-library/react";
import { useCart } from "../src/useCart.js";

const CST = { code: "CST", name: "Coaster set", paise: 15000 };
const KEY = { code: "KEY", name: "Keychain", paise: 12000 };

beforeEach(() => {
  localStorage.clear();
});

afterEach(cleanup);

describe("useCart", () => {
  it("starts with an empty cart", () => {
    const { result } = renderHook(() => useCart());
    expect(result.current.lines, "a new cart should have no lines").toEqual([]);
    expect(result.current.count, "a new cart should count 0 things").toBe(0);
    expect(result.current.total, "a new cart should cost 0 paise").toBe(0);
  });

  it("adds a new item as a line with qty 1", () => {
    const { result } = renderHook(() => useCart());
    act(() => result.current.add(KEY));
    expect(result.current.lines, "adding a keychain should make one line: the keychain with qty 1").toEqual([{ ...KEY, qty: 1 }]);
  });

  it("raises the qty when the same item is added again", () => {
    const { result } = renderHook(() => useCart());
    act(() => result.current.add(KEY));
    act(() => result.current.add(KEY));
    expect(result.current.lines.length, "two keychains should still be one line").toBe(1);
    expect(result.current.lines[0].qty, "that line should have qty 2").toBe(2);
  });

  it("counts both adds when they happen in the same click", () => {
    const { result } = renderHook(() => useCart());
    act(() => {
      result.current.add(KEY);
      result.current.add(KEY);
      result.current.add(CST);
    });
    expect(result.current.lines, "Buy 2 keychains plus a coaster set should give KEY x2 then CST x1 (use the updater form)").toEqual([
      { ...KEY, qty: 2 },
      { ...CST, qty: 1 },
    ]);
  });

  it("removes a line by its code", () => {
    const { result } = renderHook(() => useCart());
    act(() => result.current.add(KEY));
    act(() => result.current.add(CST));
    act(() => result.current.remove("KEY"));
    expect(result.current.lines, "after removing KEY only the coaster set should be left").toEqual([{ ...CST, qty: 1 }]);
  });

  it("works out the count and the total in paise", () => {
    const { result } = renderHook(() => useCart());
    act(() => result.current.add(CST));
    act(() => result.current.add(CST));
    act(() => result.current.add(KEY));
    expect(result.current.count, "2 coaster sets and 1 keychain are 3 things").toBe(3);
    expect(result.current.total, "2 x 15000 + 12000 is 42000 paise").toBe(42000);
  });

  it("saves the cart in localStorage", () => {
    const { result } = renderHook(() => useCart());
    act(() => result.current.add(KEY));
    const saved = JSON.parse(localStorage.getItem("dhaaga-cart") ?? "null");
    expect(saved, "the lines should be saved as JSON under dhaaga-cart").toEqual([{ ...KEY, qty: 1 }]);
  });

  it("uses the storage key it is given", () => {
    const { result } = renderHook(() => useCart("gift-cart"));
    act(() => result.current.add(CST));
    const saved = JSON.parse(localStorage.getItem("gift-cart") ?? "null");
    expect(saved, "useCart(\"gift-cart\") should save under gift-cart").toEqual([{ ...CST, qty: 1 }]);
  });

  it("starts from the saved cart after a refresh", () => {
    localStorage.setItem("dhaaga-cart", JSON.stringify([{ ...CST, qty: 2 }]));
    const { result } = renderHook(() => useCart());
    expect(result.current.lines, "the cart should start with the 2 coaster sets that were saved").toEqual([{ ...CST, qty: 2 }]);
    expect(result.current.count, "the saved cart has 2 things in it").toBe(2);
  });

  it("gives each component its own cart", () => {
    const grid = renderHook(() => useCart());
    const drawer = renderHook(() => useCart());
    act(() => grid.result.current.add(KEY));
    expect(grid.result.current.count, "the cart that was added to should count 1").toBe(1);
    expect(drawer.result.current.count, "the other component's cart should still be empty: hooks share logic, not state").toBe(0);
  });
});
