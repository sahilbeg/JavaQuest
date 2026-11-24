import { describe, it, expect } from "vitest";
import { OutOfStockError, reserve, tryReserve } from "../src/stock.js";

function golu() {
  return { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 3 };
}

function bunny() {
  return { id: "DC-1047", name: "Off-white bunny", paise: 45000, stock: 0 };
}

/** Runs fn and gives back what it threw, or null when it didn't throw. */
function thrownBy(fn) {
  try {
    fn();
  } catch (error) {
    return error;
  }
  return null;
}

describe("OutOfStockError", () => {
  it("is an Error with a clear name, message and item id", () => {
    const error = new OutOfStockError(golu(), 5);
    expect(error instanceof Error, "an OutOfStockError should also be an Error").toBe(true);
    expect(error.name, "the error's name should be OutOfStockError").toBe("OutOfStockError");
    expect(error.message, "the message should say how many are left and how many were asked for").toBe(
      "Only 3 of DC-1042 left, but 5 were asked for",
    );
    expect(error.itemId, "the error should remember the item's id").toBe("DC-1042");
  });
});

describe("reserve", () => {
  it("returns the item with its stock lowered", () => {
    expect(reserve(golu(), 2), "reserving 2 of 3 Golus should leave 1").toEqual({ ...golu(), stock: 1 });
  });

  it("can reserve the very last items", () => {
    const result = reserve(golu(), 3);
    expect(result && result.stock, "reserving all 3 Golus should leave 0").toBe(0);
  });

  it("does not change the item it was given", () => {
    const item = golu();
    reserve(item, 2);
    expect(item.stock, "the original item must still say 3 in stock").toBe(3);
  });

  it("throws an OutOfStockError when there isn't enough stock", () => {
    const error = thrownBy(() => reserve(golu(), 4));
    expect(error instanceof OutOfStockError, "asking for 4 of 3 should throw an OutOfStockError").toBe(true);
    expect(error && error.message, "the message should explain the shortage").toBe("Only 3 of DC-1042 left, but 4 were asked for");
  });

  it("throws an OutOfStockError for an item with no stock", () => {
    const error = thrownBy(() => reserve(bunny(), 1));
    expect(error instanceof OutOfStockError, "the bunny is sold out, so even 1 should throw").toBe(true);
  });

  it("throws a RangeError for a quantity that isn't a whole number above 0", () => {
    for (const qty of [0, -1, 1.5, "2"]) {
      const error = thrownBy(() => reserve(golu(), qty));
      expect(error instanceof RangeError, `a quantity of ${JSON.stringify(qty)} should throw a RangeError`).toBe(true);
      expect(error && error.message, "the RangeError should say what a quantity must be").toBe(
        "qty must be a whole number above 0",
      );
    }
  });

  it("checks the quantity before the stock", () => {
    const error = thrownBy(() => reserve(bunny(), 0));
    expect(error instanceof RangeError, "a quantity of 0 is a RangeError, even for a sold-out item").toBe(true);
  });
});

describe("tryReserve", () => {
  it("returns ok and the new item when it works", () => {
    expect(tryReserve(golu(), 2), "reserving 2 Golus should work").toEqual({ ok: true, item: { ...golu(), stock: 1 } });
  });

  it("returns a kind message when stock runs out", () => {
    expect(tryReserve(golu(), 5), "asking for 5 of 3 should give ok: false and the error's message").toEqual({
      ok: false,
      message: "Only 3 of DC-1042 left, but 5 were asked for",
    });
  });

  it("throws other errors again instead of hiding them", () => {
    const error = thrownBy(() => tryReserve(golu(), 0));
    expect(error instanceof RangeError, "a quantity of 0 is a bug, so tryReserve should throw the RangeError again").toBe(true);
  });
});
