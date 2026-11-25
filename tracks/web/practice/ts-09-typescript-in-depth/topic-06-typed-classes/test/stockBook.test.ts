import { describe, it, expect } from "vitest";
import { StockBook } from "../src/stockBook.ts";

describe("StockBook", () => {
  it("knows which shop it belongs to", () => {
    const book = new StockBook("FC Road stall");
    expect(book.shop, "the book should remember its shop's name").toBe("FC Road stall");
  });

  it("says 0 for an item it has never received", () => {
    const book = new StockBook("FC Road stall");
    expect(book.stockOf("DC-1042"), "no elephants have arrived yet, so stock is 0").toBe(0);
  });

  it("adds deliveries together", () => {
    const book = new StockBook("FC Road stall");
    book.receive("DC-1042", 3);
    book.receive("DC-1042", 2);
    expect(book.stockOf("DC-1042"), "3 elephants and then 2 more make 5").toBe(5);
  });

  it("refuses a delivery of 0, a negative number or a fraction", () => {
    const book = new StockBook("FC Road stall");
    expect(() => book.receive("DC-1042", 0), "a delivery of 0 units should throw").toThrow("Quantity must be a whole number above 0");
    expect(() => book.receive("DC-1042", -2), "a delivery of -2 units should throw").toThrow("Quantity must be a whole number above 0");
    expect(() => book.receive("DC-1042", 1.5), "half an elephant can't be delivered").toThrow("Quantity must be a whole number above 0");
    expect(book.stockOf("DC-1042"), "a refused delivery should change nothing").toBe(0);
  });

  it("sells when there is enough stock", () => {
    const book = new StockBook("Aundh shop");
    book.receive("DC-1042", 3);
    expect(book.sell("DC-1042", 2), "selling 2 of 3 elephants should work").toBe(true);
    expect(book.stockOf("DC-1042"), "1 elephant should be left").toBe(1);
  });

  it("refuses to sell more than it has, and changes nothing", () => {
    const book = new StockBook("Aundh shop");
    book.receive("DC-1042", 1);
    expect(book.sell("DC-1042", 3), "selling 3 elephants when there is 1 should return false").toBe(false);
    expect(book.stockOf("DC-1042"), "stock must never go below 0: still 1 elephant").toBe(1);
  });

  it("can sell the very last unit", () => {
    const book = new StockBook("Aundh shop");
    book.receive("DC-1044", 2);
    expect(book.sell("DC-1044", 2), "selling both keychains should work").toBe(true);
    expect(book.stockOf("DC-1044"), "no keychains are left").toBe(0);
  });

  it("adds up every unit in the shop", () => {
    const book = new StockBook("Aundh shop");
    book.receive("DC-1042", 3);
    book.receive("DC-1044", 8);
    book.sell("DC-1044", 1);
    expect(book.totalUnits, "3 elephants and 7 keychains make 10 units").toBe(10);
  });

  it("keeps each shop's counts separate", () => {
    const stall = new StockBook("FC Road stall");
    const shop = new StockBook("Aundh shop");
    stall.receive("DC-1042", 4);
    expect(shop.stockOf("DC-1042"), "a delivery to the stall must not show up at the Aundh shop").toBe(0);
  });
});
