import { describe, it, expect } from "vitest";
import { makeOrderNumbers, makeStockCounter, makeNumberedLabels } from "../src/counters.js";

describe("makeOrderNumbers", () => {
  it("starts at the number it was given", () => {
    const nextOrder = makeOrderNumbers(17);
    expect(nextOrder(), "the first id from makeOrderNumbers(17) should be order-17").toBe("order-17");
  });

  it("gives the next number on every call", () => {
    const nextOrder = makeOrderNumbers(17);
    nextOrder();
    expect(nextOrder(), "the second call should give order-18").toBe("order-18");
    expect(nextOrder(), "the third call should give order-19").toBe("order-19");
  });

  it("keeps two counters apart", () => {
    const stall = makeOrderNumbers(17);
    const shop = makeOrderNumbers(500);
    stall();
    stall();
    expect(shop(), "a second counter should start at its own number, not continue the first").toBe("order-500");
    expect(stall(), "the first counter should carry on from where it was").toBe("order-19");
  });
});

describe("makeStockCounter", () => {
  it("starts with the stock it was given", () => {
    const golu = makeStockCounter(3);
    expect(golu.left(), "a counter made with 3 should have 3 left").toBe(3);
  });

  it("sells one item at a time", () => {
    const golu = makeStockCounter(3);
    expect(golu.sell(), "selling when there is stock should return true").toBe(true);
    expect(golu.left(), "after one sale, 2 should be left").toBe(2);
  });

  it("refuses to sell when the count is 0", () => {
    const bunny = makeStockCounter(1);
    bunny.sell();
    expect(bunny.sell(), "selling with nothing left should return false").toBe(false);
    expect(bunny.left(), "a refused sale must not take the count below 0").toBe(0);
  });

  it("adds stock with restock", () => {
    const bags = makeStockCounter(0);
    bags.restock(5);
    expect(bags.left(), "restocking 5 bags onto 0 should leave 5").toBe(5);
    expect(bags.sell(), "after a restock, a sale should work again").toBe(true);
  });

  it("keeps the count private", () => {
    const owl = makeStockCounter(4);
    expect(Object.keys(owl).sort().join(","), "the object should only hold left, restock and sell").toBe("left,restock,sell");
    const golu = makeStockCounter(3);
    golu.count = 100;
    golu.sell();
    expect(golu.left(), "setting a count property from outside must not change the real count").toBe(2);
  });

  it("keeps two counters apart", () => {
    const golu = makeStockCounter(3);
    const owl = makeStockCounter(3);
    golu.sell();
    golu.sell();
    expect(owl.left(), "selling Golu must not change the owl keychain's count").toBe(3);
  });
});

describe("makeNumberedLabels", () => {
  const names = ["Golu the elephant", "Marigold coaster set", "Indigo tote bag"];

  it("makes one label function per name", () => {
    expect(makeNumberedLabels(names).length, "three names should make three functions").toBe(3);
  });

  it("numbers each label from 1 with its own name", () => {
    const labels = makeNumberedLabels(names);
    expect(labels[0](), "the first function should return the first name, numbered 1").toBe("1. Golu the elephant");
    expect(labels[2](), "the third function should return the third name, numbered 3").toBe("3. Indigo tote bag");
  });
});
