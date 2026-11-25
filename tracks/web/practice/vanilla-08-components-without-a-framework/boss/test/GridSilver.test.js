import { describe, it, expect } from "vitest";
import { openGrid, type, cardFor, inputFor, ids, withStock, golu, coasters, keychain, booties } from "./grid.js";

describe("Silver: a refresh changes only what changed", () => {
  it("keeps Golu's card and shows its new stock", () => {
    const grid = openGrid();
    grid.update([golu, coasters]);
    const card = cardFor("DC-1042");
    grid.update([withStock(golu, 2), coasters]);
    expect(cardFor("DC-1042"), "Golu should still be shown by the very same <li>").toBe(card);
    expect(card.querySelector(".card__stock")?.textContent, "Golu's card should now say Only 2 left").toBe("Only 2 left");
  });

  it("disables Add on a kept card when it sells out", () => {
    const grid = openGrid();
    grid.update([golu, coasters]);
    const card = cardFor("DC-1042");
    grid.update([withStock(golu, 0), coasters]);
    expect(cardFor("DC-1042"), "Golu's card should be kept when it sells out").toBe(card);
    expect(card.querySelector(".card__stock")?.textContent, "a sold-out card should say Sold out").toBe("Sold out");
    expect(card.querySelector(".card__add")?.disabled, "a sold-out card's Add button should be disabled").toBe(true);
  });

  it("keeps focus in the box being typed in", () => {
    const grid = openGrid();
    grid.update([golu, coasters, keychain]);
    type("DC-1042", "Aar");
    const box = inputFor("DC-1042");
    grid.update([withStock(golu, 3), withStock(coasters, 11), keychain]);
    expect(document.activeElement, "the customer's cursor should still be in Golu's name box").toBe(box);
  });

  it("adds a new product at its place and removes one that is gone", () => {
    const grid = openGrid();
    grid.update([golu, coasters, keychain]);
    type("DC-1042", "Aarav");
    const gone = cardFor("DC-1001");
    grid.update([golu, booties, keychain]);
    expect(ids(), "the grid should show Golu, the booties, then the keychain").toEqual(["DC-1042", "DC-1020", "DC-1013"]);
    expect(gone.isConnected, "the coasters' card should be removed").toBe(false);
    expect(inputFor("DC-1042")?.value, "Golu's typed name should survive the change").toBe("Aarav");
  });

  it("follows a new order without losing the typed name", () => {
    const grid = openGrid();
    grid.update([golu, coasters, keychain]);
    type("DC-1042", "Aarav");
    const card = cardFor("DC-1042");
    grid.update([keychain, golu, coasters]);
    expect(ids(), "the cards should follow the new order: keychain, Golu, coasters").toEqual(["DC-1013", "DC-1042", "DC-1001"]);
    expect(cardFor("DC-1042"), "Golu's card should be the same element after moving").toBe(card);
    expect(inputFor("DC-1042")?.value, "Golu's typed name should survive the move").toBe("Aarav");
  });
});
