import { describe, it, expect } from "vitest";
import { groupOrdersByDay } from "../src/groupOrdersByDay.js";

const ORDERS = [
  { id: "order-17", placedAt: "2026-11-08T10:15:00+05:30", paise: 45000 },
  { id: "order-18", placedAt: "2026-11-08T18:40:00+05:30", paise: 15000 },
  { id: "order-19", placedAt: "2026-11-07T12:00:00+05:30", paise: 89900 },
  { id: "order-20", placedAt: "2026-11-09T02:10:00+05:30", paise: 12000 },
];

describe("Round 1: groupOrdersByDay", () => {
  it("returns an empty list for no orders", () => {
    expect(groupOrdersByDay([]), "no orders means no days").toEqual([]);
  });

  it("counts the very first order", () => {
    expect(groupOrdersByDay([ORDERS[0]]), "a single order on 8 November should make one day, with that order in it. Does the loop start at the first order?").toEqual([
      { day: "2026-11-08", count: 1, totalPaise: 45000 },
    ]);
  });

  it("adds up every order on the same day", () => {
    expect(groupOrdersByDay([ORDERS[0], ORDERS[1]]), "order-17 and order-18 are both on 8 November: 2 orders, 45000 + 15000 = 60000 paise").toEqual([
      { day: "2026-11-08", count: 2, totalPaise: 60000 },
    ]);
  });

  it("keeps an early-morning order on its own day in India", () => {
    expect(groupOrdersByDay([ORDERS[3]]), "order-20 was placed at 02:10 on 9 November in India. toISOString() is UTC, where it is still 8 November. Use the date written in placedAt").toEqual([
      { day: "2026-11-09", count: 1, totalPaise: 12000 },
    ]);
  });

  it("lists the days oldest first", () => {
    expect(groupOrdersByDay(ORDERS).map((group) => group.day), "the days should be sorted, oldest first").toEqual(["2026-11-07", "2026-11-08", "2026-11-09"]);
    expect(groupOrdersByDay(ORDERS), "the full answer for all four orders").toEqual([
      { day: "2026-11-07", count: 1, totalPaise: 89900 },
      { day: "2026-11-08", count: 2, totalPaise: 60000 },
      { day: "2026-11-09", count: 1, totalPaise: 12000 },
    ]);
  });

  it("leaves the orders it was given unchanged", () => {
    const before = JSON.parse(JSON.stringify(ORDERS));
    groupOrdersByDay(ORDERS);
    expect(ORDERS, "groupOrdersByDay must not change or reorder the orders passed in").toEqual(before);
  });
});
