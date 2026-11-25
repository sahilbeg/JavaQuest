import { describe, it, expect } from "vitest";
import { mergeCarts } from "../src/mergeCarts.ts";

const golu = (qty: number, paise = 45000) => ({ id: "DC-1042", name: "Golu the elephant", paise, qty });
const keychain = (qty: number) => ({ id: "DC-1201", name: "Mint keychain", paise: 12000, qty });
const tote = (qty: number) => ({ id: "DC-1301", name: "Rani pink tote bag", paise: 89900, qty });

describe("Round 1: mergeCarts", () => {
  it("keeps the lines that are only in one cart", () => {
    expect(mergeCarts([golu(1)], [keychain(2)]), "Golu from the saved cart and the keychain from this browser should both stay").toEqual([golu(1), keychain(2)]);
  });

  it("adds up the quantities of an item in both carts", () => {
    expect(mergeCarts([golu(1)], [golu(2)]), "1 Golu saved and 2 in the browser make one line of 3").toEqual([golu(3)]);
  });

  it("never goes above 5 of one item", () => {
    expect(mergeCarts([golu(4)], [golu(3)]), "4 + 3 Golus is more than MAX_QTY, so the line should stop at 5").toEqual([golu(5)]);
  });

  it("uses the browser's newer name and price", () => {
    expect(mergeCarts([golu(1, 40000)], [golu(1)]), "the saved cart has last month's price; the browser's 45000 paise is newer and should win").toEqual([golu(2)]);
  });

  it("lists the saved lines first, then the new ones", () => {
    expect(mergeCarts([tote(1), golu(1)], [keychain(1), golu(1)]), "saved lines keep their order, then lines only in this browser follow").toEqual([tote(1), golu(2), keychain(1)]);
  });

  it("drops lines with nothing in them", () => {
    expect(mergeCarts([golu(0)], [keychain(1), tote(0)]), "a line with qty 0 is not in the cart, so it shouldn't come back").toEqual([keychain(1)]);
  });

  it("leaves both carts it was given unchanged", () => {
    const saved = [golu(1), tote(1)];
    const current = [golu(2), keychain(1)];
    mergeCarts(saved, current);
    expect(saved, "mergeCarts must not change the saved cart: build new lines instead of changing qty").toEqual([golu(1), tote(1)]);
    expect(current, "mergeCarts must not change the browser's cart").toEqual([golu(2), keychain(1)]);
  });
});
