import { describe, it, expect } from "vitest";
import { updateItem, toCard, createItem, stockByCode } from "../src/items.ts";
import type { Item } from "../src/items.ts";

function golu(): Item {
  return { id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 };
}

describe("updateItem", () => {
  it("applies a new price", () => {
    const updated = updateItem(golu(), { paise: 42000 });
    expect(updated.paise, "the new price should be 42000 paise").toBe(42000);
    expect(updated.name, "fields that weren't changed should stay the same").toBe("Golu the elephant");
  });

  it("applies several changes at once", () => {
    const updated = updateItem(golu(), { colour: "Mint", stock: 7 });
    expect(updated.colour, "the colour should now be Mint").toBe("Mint");
    expect(updated.stock, "the stock should now be 7").toBe(7);
    expect(updated.id, "the id must never change").toBe("DC-1042");
  });

  it("never changes the item it was given", () => {
    const original = golu();
    const updated = updateItem(original, { paise: 42000 });
    expect(original.paise, "the original item should still cost 45000 paise").toBe(45000);
    expect(updated === original, "updateItem should return a new object, not the same one").toBe(false);
  });
});

describe("toCard", () => {
  it("keeps the id, name and price", () => {
    const card = toCard(golu());
    expect(card, "the card should hold Golu's id, name and price").toEqual({ id: "DC-1042", name: "Golu the elephant", paise: 45000 });
  });

  it("has no other fields", () => {
    expect(Object.keys(toCard(golu())).sort(), "a card has exactly three fields: id, name and paise").toEqual(["id", "name", "paise"]);
  });
});

describe("createItem", () => {
  it("gives the new item its DC- id", () => {
    const item = createItem({ code: "BAG", name: "Marigold tote bag", colour: "Marigold", paise: 89900, stock: 2 }, 1050);
    expect(item.id, "item number 1050 should get the id DC-1050").toBe("DC-1050");
    expect(item.name, "the rest of the item should be copied").toBe("Marigold tote bag");
    expect(item.paise, "the price should be copied").toBe(89900);
  });
});

describe("stockByCode", () => {
  it("adds up the stock for each code", () => {
    const items: Item[] = [
      golu(),
      { id: "DC-1046", code: "AMI", name: "Rani pink parrot", colour: "Rani pink", paise: 45000, stock: 2 },
      { id: "DC-1044", code: "KEY", name: "Mint keychain", colour: "Mint", paise: 12000, stock: 8 },
    ];
    const stock = stockByCode(items);
    expect(stock.AMI, "3 elephants and 2 parrots make 5 AMI").toBe(5);
    expect(stock.KEY, "there are 8 keychains").toBe(8);
    expect(stock.BTY, "no booties are listed, so BTY should be 0").toBe(0);
  });

  it("lists every code, even for an empty catalogue", () => {
    expect(stockByCode([]), "every code should be there with 0").toEqual({ CST: 0, KEY: 0, AMI: 0, BAG: 0, BTY: 0 });
  });
});
