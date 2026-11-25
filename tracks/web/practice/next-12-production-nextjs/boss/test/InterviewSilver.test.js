import { describe, it, expect } from "vitest";
import { addToCart } from "../src/app/cart/actions.js";

function form(id, qty) {
  const data = new FormData();
  data.set("id", id);
  data.set("qty", qty);
  return data;
}

function copy(value) {
  return JSON.parse(JSON.stringify(value));
}

describe("Round 2: the addToCart server action", () => {
  it("adds a new line with a number quantity", async () => {
    const next = await addToCart({ lines: [], error: "" }, form("DC-1042", "2"));
    expect(next, "adding 2 Golus to an empty cart should give one line with qty 2, a number (TODO 3)").toEqual({ lines: [{ id: "DC-1042", qty: 2 }], error: "" });
  });

  it("adds to the line that's already there instead of making a second one", async () => {
    const prev = { lines: [{ id: "DC-1101", qty: 1 }, { id: "DC-1042", qty: 2 }], error: "" };
    const next = await addToCart(prev, form("DC-1042", "3"));
    expect(next, "2 Golus plus 3 should make one line of 5, not \"23\" and not two lines (TODO 3)").toEqual({
      lines: [{ id: "DC-1101", qty: 1 }, { id: "DC-1042", qty: 5 }],
      error: "",
    });
  });

  it("leaves the previous state exactly as it was", async () => {
    const prev = { lines: [{ id: "DC-1042", qty: 2 }], error: "" };
    const before = copy(prev);
    await addToCart(prev, form("DC-1042", "1"));
    await addToCart(prev, form("DC-1101", "1"));
    expect(prev, "addToCart must return new arrays and objects, never change prevState (TODO 3)").toEqual(before);
  });

  it("refuses an item that doesn't exist", async () => {
    const prev = { lines: [{ id: "DC-1042", qty: 1 }], error: "" };
    expect(await addToCart(prev, form("DC-9999", "1")), "an unknown id should keep the lines and say Unknown item (TODO 2)").toEqual({
      lines: [{ id: "DC-1042", qty: 1 }],
      error: "Unknown item",
    });
  });

  it("refuses quantities that aren't a whole number from 1 to 5", async () => {
    for (const qty of ["0", "6", "1.5", "x", ""]) {
      expect(await addToCart({ lines: [], error: "" }, form("DC-1042", qty)), `qty "${qty}" should be refused (TODO 2)`).toEqual({
        lines: [],
        error: "Pick a quantity from 1 to 5",
      });
    }
  });

  it("never puts more in the cart than there is in stock", async () => {
    const prev = { lines: [{ id: "DC-1042", qty: 5 }], error: "" };
    expect(await addToCart(prev, form("DC-1042", "3")), "5 Golus in the cart plus 3 is 8, but only 7 exist (TODO 2)").toEqual({
      lines: [{ id: "DC-1042", qty: 5 }],
      error: "Only 7 left",
    });
    expect(await addToCart({ lines: [], error: "" }, form("DC-1205", "1")), "the Indigo keychain has 0 in stock (TODO 2)").toEqual({
      lines: [],
      error: "Sold out",
    });
    expect(await addToCart({ lines: [], error: "" }, form("DC-1301", "2")), "both tote bags in stock can be added (TODO 2)").toEqual({
      lines: [{ id: "DC-1301", qty: 2 }],
      error: "",
    });
  });
});
