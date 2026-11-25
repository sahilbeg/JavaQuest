import { describe, it, expect, beforeEach } from "vitest";
import { reconcile } from "../src/keyed.js";

let list;
let made;

beforeEach(() => {
  document.body.innerHTML = `<ul id="queue"></ul>`;
  list = document.querySelector("#queue");
  made = [];
});

const options = {
  key: (order) => order.id,
  create: (order) => {
    const row = document.createElement("li");
    row.innerHTML = `<input type="checkbox" class="packed"> <span class="what"></span>`;
    made.push(order.id);
    return row;
  },
  update: (row, order) => {
    row.querySelector(".what").textContent = `${order.id}: ${order.text}`;
  },
};

const o17 = { id: "order-17", text: "2 × Golu the elephant" };
const o18 = { id: "order-18", text: "1 × Indigo tote bag" };
const o19 = { id: "order-19", text: "4 × Marigold coaster set" };
const o20 = { id: "order-20", text: "1 × Rani pink keychain" };

const keys = () => [...list.children].map((row) => row.dataset.key);
const rowFor = (id) => list.querySelector(`[data-key="${id}"]`);

describe("reconcile", () => {
  it("draws one row per item, in order, each with its data-key", () => {
    reconcile(list, [o17, o18], options);
    expect(keys(), "the rows should be keyed order-17 then order-18").toEqual(["order-17", "order-18"]);
    expect(rowFor("order-18")?.querySelector(".what")?.textContent, "update should fill each row's text").toBe("order-18: 1 × Indigo tote bag");
  });

  it("keeps the same row element for a key that stays", () => {
    reconcile(list, [o17, o18], options);
    const before = rowFor("order-17");
    reconcile(list, [o17, o18, o19], options);
    expect(rowFor("order-17"), "order-17 should still be shown by the very same <li>, not a new one").toBe(before);
  });

  it("keeps the tick in a row that stays", () => {
    reconcile(list, [o17, o18], options);
    rowFor("order-17").querySelector(".packed").checked = true;
    reconcile(list, [o17, o18, o19], options);
    expect(rowFor("order-17")?.querySelector(".packed")?.checked, "Raju's tick on order-17 should survive a new order arriving").toBe(true);
  });

  it("makes rows only for new keys", () => {
    reconcile(list, [o17, o18], options);
    reconcile(list, [o17, o18, o19], options);
    expect(made, "create should run for order-17 and order-18 once, then only for order-19").toEqual(["order-17", "order-18", "order-19"]);
  });

  it("updates the text of a kept row", () => {
    reconcile(list, [o17], options);
    const before = rowFor("order-17");
    reconcile(list, [{ ...o17, text: "3 × Golu the elephant" }], options);
    expect(rowFor("order-17"), "the row should be kept").toBe(before);
    expect(before.querySelector(".what").textContent, "update should run on kept rows too").toBe("order-17: 3 × Golu the elephant");
  });

  it("removes rows whose keys are gone", () => {
    reconcile(list, [o17, o18, o19], options);
    const gone = rowFor("order-18");
    reconcile(list, [o17, o19], options);
    expect(keys(), "after order-18 leaves, the rows should be order-17 then order-19").toEqual(["order-17", "order-19"]);
    expect(gone.isConnected, "order-18's row should be taken off the page").toBe(false);
  });

  it("moves kept rows into the new order without remaking them", () => {
    reconcile(list, [o17, o18, o19], options);
    const rows = { 17: rowFor("order-17"), 18: rowFor("order-18"), 19: rowFor("order-19") };
    reconcile(list, [o19, o17, o18], options);
    expect(keys(), "the rows should follow the new order: 19, 17, 18").toEqual(["order-19", "order-17", "order-18"]);
    const same = [...list.children].map((row, index) => row === [rows[19], rows[17], rows[18]][index]);
    expect(same, "the moved rows should be the very same three elements").toEqual([true, true, true]);
    expect(made.length, "moving rows should make no new ones").toBe(3);
  });

  it("adds a new row in the middle at its place", () => {
    reconcile(list, [o17, o19], options);
    reconcile(list, [o17, o20, o19], options);
    expect(keys(), "order-20 should appear between order-17 and order-19").toEqual(["order-17", "order-20", "order-19"]);
  });

  it("keeps focus in a row that stays in place", () => {
    reconcile(list, [o17, o18], options);
    const box = rowFor("order-17").querySelector(".packed");
    box.focus();
    reconcile(list, [o17, o18, o19, o20], options);
    expect(document.activeElement, "focus should stay on order-17's checkbox while new orders arrive").toBe(box);
  });

  it("empties the list when there are no items", () => {
    reconcile(list, [o17, o18], options);
    reconcile(list, [], options);
    expect(list.children.length, "with no orders, the list should be empty").toBe(0);
  });
});
