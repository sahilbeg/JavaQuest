import { describe, it, expect } from "vitest";
import { uniqueColours, stockByColour, soldOutColours, restock } from "../src/stockroom.js";

function catalogue() {
  return [
    { id: "DC-1042", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 },
    { id: "DC-1043", name: "Marigold coaster set", colour: "Marigold", paise: 15000, stock: 12 },
    { id: "DC-1044", name: "Mint owl keychain", colour: "Mint", paise: 12000, stock: 0 },
    { id: "DC-1045", name: "Indigo tote bag", colour: "Indigo", paise: 89900, stock: 5 },
    { id: "DC-1046", name: "Rani pink baby booties", colour: "Rani pink", paise: 35000, stock: 4 },
    { id: "DC-1047", name: "Off-white bunny", colour: "Off-white", paise: 45000, stock: 0 },
  ];
}

describe("uniqueColours", () => {
  it("lists each colour once, in the order it first appears", () => {
    expect(uniqueColours(catalogue()).join(", "), "Indigo appears twice in the catalogue but should be listed once").toBe(
      "Indigo, Marigold, Mint, Rani pink, Off-white",
    );
  });

  it("returns an array", () => {
    expect(Array.isArray(uniqueColours(catalogue())), "the colour buttons need an array, not a Set").toBe(true);
  });
});

describe("stockByColour", () => {
  it("returns a Map", () => {
    expect(stockByColour(catalogue()) instanceof Map, "stockByColour should return a Map").toBe(true);
  });

  it("adds up the stock for each colour", () => {
    const totals = stockByColour(catalogue());
    expect(totals.get("Indigo"), "Golu (3) and the tote bag (5) make 8 Indigo items").toBe(8);
    expect(totals.get("Marigold"), "there are 12 Marigold coaster sets").toBe(12);
    expect(totals.get("Mint"), "the Mint keychain is sold out, so Mint has 0").toBe(0);
  });

  it("has one entry per colour, in first-seen order", () => {
    const totals = stockByColour(catalogue());
    expect([...totals.keys()].join(", "), "five colours, in the order they first appear").toBe(
      "Indigo, Marigold, Mint, Rani pink, Off-white",
    );
  });

  it("is empty for an empty catalogue", () => {
    expect(stockByColour([]).size, "no items means no colours").toBe(0);
  });
});

describe("soldOutColours", () => {
  it("lists the colours with no stock left", () => {
    expect(soldOutColours(catalogue()).join(", "), "Mint and Off-white have nothing left").toBe("Mint, Off-white");
  });

  it("counts every item of a colour before calling it sold out", () => {
    const items = [
      { id: "DC-1044", colour: "Mint", stock: 0 },
      { id: "DC-1050", colour: "Mint", stock: 2 },
    ];
    expect(soldOutColours(items).length, "one Mint item is sold out, but another has 2, so Mint is not sold out").toBe(0);
  });
});

describe("restock", () => {
  it("adds grams to a colour that is already there", () => {
    const yarn = new Map([["Marigold", 460], ["Indigo", 320]]);
    restock(yarn, "Indigo", 200);
    expect(yarn.get("Indigo"), "320 g of Indigo plus 200 g is 520 g").toBe(520);
    expect(yarn.get("Marigold"), "Marigold must not change").toBe(460);
  });

  it("starts a new colour from 0", () => {
    const yarn = new Map([["Marigold", 460]]);
    restock(yarn, "Mint", 250);
    expect(yarn.get("Mint"), "a new colour should start at 0, so 250 g of Mint is 250").toBe(250);
    expect(yarn.size, "the register should now hold two colours").toBe(2);
  });

  it("returns the same Map it was given", () => {
    const yarn = new Map();
    expect(restock(yarn, "Rani pink", 100), "restock should return the Map it changed").toBe(yarn);
  });
});
