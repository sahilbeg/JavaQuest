import { describe, it, expect } from "vitest";
import { openGrid, type, inputFor, ids, withStock, golu, coasters, keychain } from "./grid.js";

describe("Bronze: a refresh keeps what customers typed", () => {
  it("keeps the name typed on Golu's card", () => {
    const grid = openGrid();
    grid.update([golu, coasters, keychain]);
    type("DC-1042", "Aarav");
    grid.update([withStock(golu, 3), coasters, keychain]);
    expect(inputFor("DC-1042")?.value, "after a stock refresh, Golu's name box should still say Aarav").toBe("Aarav");
  });

  it("keeps the names typed on every card", () => {
    const grid = openGrid();
    grid.update([golu, coasters, keychain]);
    type("DC-1042", "Aarav");
    type("DC-1013", "Meher");
    grid.update([golu, withStock(coasters, 11), keychain]);
    expect(inputFor("DC-1042")?.value, "Golu's box should still say Aarav").toBe("Aarav");
    expect(inputFor("DC-1013")?.value, "the keychain's box should still say Meher").toBe("Meher");
  });

  it("still shows every product after a refresh", () => {
    const grid = openGrid();
    grid.update([golu, coasters, keychain]);
    grid.update([withStock(golu, 3), coasters, keychain]);
    expect(ids(), "the grid should still show Golu, the coasters and the keychain, in order").toEqual(["DC-1042", "DC-1001", "DC-1013"]);
  });
});
