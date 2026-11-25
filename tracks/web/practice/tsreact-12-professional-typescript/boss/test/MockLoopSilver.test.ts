import { describe, it, expect, beforeEach, afterEach } from "vitest";
import { renderHook, act, cleanup } from "@testing-library/react";
import { useStoredState } from "../src/useStoredState.ts";
import { isCartLines } from "../src/types.ts";

const GUEST = "dhaaga-cart-guest";
const MEERA = "dhaaga-cart-9876543210";
const GOLU = { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 1 };
const TOTE = { id: "DC-1301", name: "Rani pink tote bag", paise: 89900, qty: 2 };

beforeEach(() => {
  localStorage.clear();
});
afterEach(cleanup);

function renderCart(key: string) {
  return renderHook(({ storageKey }) => useStoredState(storageKey, [], isCartLines), { initialProps: { storageKey: key } });
}

describe("Round 2: useStoredState", () => {
  it("starts with the initial value when nothing is stored", () => {
    const { result } = renderCart(GUEST);
    expect(result.current[0], "with nothing stored, the cart should start empty").toEqual([]);
  });

  it("reads back a good stored value", () => {
    localStorage.setItem(GUEST, JSON.stringify([GOLU]));
    const { result } = renderCart(GUEST);
    expect(result.current[0], "a stored cart with one Golu should come back").toEqual([GOLU]);
  });

  it("ignores stored data with the wrong shape", () => {
    localStorage.setItem(GUEST, JSON.stringify({ id: "DC-1042" }));
    expect(renderCart(GUEST).result.current[0], "an object is not a list of cart lines: check it with isValid instead of trusting it with as (TODO 2)").toEqual([]);
    cleanup();
    localStorage.setItem(GUEST, JSON.stringify([{ ...GOLU, qty: "2" }]));
    expect(renderCart(GUEST).result.current[0], "a line whose qty is the text \"2\" must be refused, or the total becomes text (TODO 2)").toEqual([]);
  });

  it("ignores stored text that isn't JSON", () => {
    localStorage.setItem(GUEST, "[{ broken");
    expect(renderCart(GUEST).result.current[0], "broken JSON should give the initial value, not crash").toEqual([]);
  });

  it("saves every change", () => {
    const { result } = renderCart(GUEST);
    act(() => result.current[1]([GOLU, TOTE]));
    expect(result.current[0], "after saving, the hook should give the new cart").toEqual([GOLU, TOTE]);
    expect(localStorage.getItem(GUEST), "the new cart should be written to localStorage as JSON under the key (TODO 3)").toBe(JSON.stringify([GOLU, TOTE]));
  });

  it("reads the other customer's cart when the key changes", () => {
    localStorage.setItem(GUEST, JSON.stringify([GOLU]));
    localStorage.setItem(MEERA, JSON.stringify([TOTE]));
    const { result, rerender } = renderCart(GUEST);
    expect(result.current[0], "the guest cart has one Golu").toEqual([GOLU]);
    rerender({ storageKey: MEERA });
    expect(result.current[0], "after Meera logs in, the hook should show her stored cart, not the guest's (TODO 3)").toEqual([TOTE]);
  });
});
