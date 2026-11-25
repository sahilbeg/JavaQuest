import { describe, it, expect, vi } from "vitest";
import { openGrid, type, inputFor, add, withStock, golu, coasters } from "./grid.js";

describe("Gold: one tap, one item", () => {
  it("calls onAdd once per press, however many refreshes came before", () => {
    const onAdd = vi.fn();
    const grid = openGrid(onAdd);
    grid.update([golu, coasters]);
    grid.update([withStock(golu, 3), coasters]);
    grid.update([withStock(golu, 2), coasters]);
    add("DC-1042");
    expect(onAdd.mock.calls.length, "one press of Add after three refreshes should call onAdd once, not three times").toBe(1);
  });

  it("sends the id and the trimmed name typed on the card", () => {
    const onAdd = vi.fn();
    const grid = openGrid(onAdd);
    grid.update([golu, coasters]);
    type("DC-1042", "  Aarav ");
    grid.update([withStock(golu, 3), coasters]);
    add("DC-1042");
    expect(onAdd.mock.calls, "Add on Golu should call onAdd({ id: \"DC-1042\", stitch: \"Aarav\" }) once").toEqual([[{ id: "DC-1042", stitch: "Aarav" }]]);
  });

  it("does nothing for a click outside the Add buttons", () => {
    const onAdd = vi.fn();
    const grid = openGrid(onAdd);
    grid.update([golu, coasters]);
    document.querySelector("#grid .card__name").click();
    expect(onAdd.mock.calls.length, "a click on a product's name should not add anything").toBe(0);
  });

  it("gives a product that comes back a fresh, empty box", () => {
    const grid = openGrid();
    grid.update([golu, coasters]);
    type("DC-1001", "Diya");
    grid.update([golu]);
    grid.update([golu, coasters]);
    expect(inputFor("DC-1001")?.value, "the coasters left and came back, so their box should start empty").toBe("");
  });
});
