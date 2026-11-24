import { describe, it, expect } from "vitest";
import { cartReducer, emptyCart } from "../src/cartReducer.js";

const KEY = { code: "KEY", name: "Keychain", paise: 12000 };
const CST = { code: "CST", name: "Coaster set", paise: 15000 };

/** A cart with 2 keychains and 1 coaster set. */
function twoLines() {
  return { lines: [{ ...KEY, qty: 2 }, { ...CST, qty: 1 }] };
}

describe("cartReducer", () => {
  it("adds a new item as a line with qty 1", () => {
    const next = cartReducer(emptyCart, { type: "added", item: KEY });
    expect(next.lines, "adding a keychain to an empty cart should give one line: KEY with qty 1").toEqual([{ ...KEY, qty: 1 }]);
  });

  it("raises the qty when the item is already in the cart", () => {
    const next = cartReducer(twoLines(), { type: "added", item: KEY });
    expect(next.lines, "adding another keychain should make it qty 3 and leave the coaster set alone").toEqual([
      { ...KEY, qty: 3 },
      { ...CST, qty: 1 },
    ]);
  });

  it("removes a line by its code", () => {
    const next = cartReducer(twoLines(), { type: "removed", code: "KEY" });
    expect(next.lines, "removing KEY should leave only the coaster set").toEqual([{ ...CST, qty: 1 }]);
  });

  it("sets a line's quantity", () => {
    const next = cartReducer(twoLines(), { type: "quantityChanged", code: "CST", qty: 5 });
    expect(next.lines, "quantityChanged to 5 should make the coaster set qty 5").toEqual([
      { ...KEY, qty: 2 },
      { ...CST, qty: 5 },
    ]);
  });

  it("removes the line when the quantity drops to 0", () => {
    const next = cartReducer(twoLines(), { type: "quantityChanged", code: "CST", qty: 0 });
    expect(next.lines, "a qty of 0 should remove the line, not leave a line with qty 0").toEqual([{ ...KEY, qty: 2 }]);
  });

  it("empties the cart", () => {
    const next = cartReducer(twoLines(), { type: "cleared" });
    expect(next.lines, "cleared should leave no lines").toEqual([]);
  });

  it("never changes the state it was given", () => {
    const state = twoLines();
    const before = JSON.stringify(state);
    const actions = [
      { type: "added", item: KEY },
      { type: "added", item: CST },
      { type: "removed", code: "KEY" },
      { type: "quantityChanged", code: "CST", qty: 4 },
      { type: "quantityChanged", code: "KEY", qty: 0 },
      { type: "cleared" },
    ];
    for (const action of actions) {
      const next = cartReducer(state, action);
      expect(next, `the "${action.type}" action should return a new state object, not the old one`).not.toBe(state);
      expect(JSON.stringify(state), `the "${action.type}" action should leave the old state exactly as it was`).toBe(before);
    }
  });

  it("throws for an action it doesn't know", () => {
    expect(
      () => cartReducer(twoLines(), { type: "couponApplied" }),
      "an unknown type should throw an Error saying Unknown action: couponApplied",
    ).toThrow("Unknown action: couponApplied");
  });
});
