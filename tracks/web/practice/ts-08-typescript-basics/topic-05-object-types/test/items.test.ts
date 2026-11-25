import { describe, it, expect } from "vitest";
import { makeItem, describeItem, describeToy, inColour, orderTotal } from "../src/items.ts";

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, colours: ["Indigo", "Off-white"] };
const tote = { id: "DC-2001", name: "Marigold tote bag", paise: 89900, colours: ["Marigold"] };
const booties = { id: "DC-3005", name: "Baby booties", paise: 35000, colours: ["Mint", "Indigo"] };

describe("Catalogue items with interfaces", () => {
  it("builds an item with all four properties, colours included", () => {
    const item = makeItem("DC-1042", "Golu the elephant", 45000, ["Indigo", "Off-white"]);
    expect(item.id, "the item's id should be DC-1042").toBe("DC-1042");
    expect(item.name, "the item's name should be Golu the elephant").toBe("Golu the elephant");
    expect(item.paise, "the item's price should be 45000 paise").toBe(45000);
    expect(item.colours, "the item should keep its colours, Indigo and Off-white").toEqual(["Indigo", "Off-white"]);
  });

  it("describes an item with its id, name and price in rupees", () => {
    expect(describeItem(golu), "Golu should read \"DC-1042 Golu the elephant: ₹450.00\"").toBe("DC-1042 Golu the elephant: ₹450.00");
    expect(describeItem(tote), "the tote bag should read \"DC-2001 Marigold tote bag: ₹899.00\"").toBe("DC-2001 Marigold tote bag: ₹899.00");
  });

  it("describes an amigurumi with its animal in brackets", () => {
    const toy = { ...golu, animal: "elephant" };
    expect(describeToy(toy), "an amigurumi adds its animal: \"DC-1042 Golu the elephant: ₹450.00 (elephant)\"").toBe(
      "DC-1042 Golu the elephant: ₹450.00 (elephant)",
    );
  });

  it("finds the items made in a colour", () => {
    const items = [golu, tote, booties];
    expect(inColour(items, "Indigo").map((item) => item.id), "Golu and the booties use Indigo, in that order").toEqual(["DC-1042", "DC-3005"]);
    expect(inColour(items, "Marigold").map((item) => item.id), "only the tote bag uses Marigold").toEqual(["DC-2001"]);
    expect(inColour(items, "Rani pink"), "nothing is made in Rani pink yet").toEqual([]);
  });

  it("totals an order of lines that each hold an item and a quantity", () => {
    const lines = [
      { item: golu, qty: 2 },
      { item: booties, qty: 1 },
    ];
    expect(orderTotal(lines), "two Golus and one pair of booties cost 90000 + 35000 = 125000 paise").toBe(125000);
    expect(orderTotal([]), "an empty order costs 0 paise").toBe(0);
  });
});
