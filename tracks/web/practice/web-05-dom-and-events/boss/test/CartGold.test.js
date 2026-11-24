import { describe, it, expect, beforeEach } from "vitest";
import { openPage, add, linesOnScreen, totalText, emptyMessageShown } from "./page.js";

beforeEach(() => {
  localStorage.clear();
});

describe("Gold: quantities survive, and bad saved data can't break the cart", () => {
  it("keeps two Golus as two after a refresh", () => {
    openPage();
    add("DC-1042");
    add("DC-1042");
    openPage();
    expect(linesOnScreen(), "two Golus should come back as one line with quantity 2: save the whole lines").toEqual([{ id: "DC-1042", qty: "2" }]);
    expect(totalText(), "two Golus total ₹900.00").toBe("₹900.00");
  });

  it("counts every quantity in the total after a refresh", () => {
    openPage();
    add("DC-1042");
    add("DC-1042");
    add("DC-1001");
    openPage();
    expect(totalText(), "two Golus and one coaster set total ₹1,050.00").toBe("₹1,050.00");
  });

  it("keeps counting after a refresh", () => {
    openPage();
    add("DC-1013");
    openPage();
    add("DC-1013");
    expect(linesOnScreen(), "a keychain from before the refresh plus one more is a quantity of 2").toEqual([{ id: "DC-1013", qty: "2" }]);
  });

  it("gives an empty cart when the saved value is not a list", () => {
    localStorage.setItem("dhaaga:cart", '{"DC-1042":2}');
    expect(() => openPage(), "valid JSON that isn't a list must not crash the page").not.toThrow();
    expect(emptyMessageShown(), "a saved object instead of a list should give an empty cart").toBe(true);
  });

  it("drops bad lines and keeps the good ones", () => {
    localStorage.setItem("dhaaga:cart", JSON.stringify([
      null,
      "DC-1001",
      { id: "DC-1042", qty: 2 },
      { id: "DC-1001", qty: "two" },
      { id: "DC-1013", qty: 0 },
      { id: "DC-1013", qty: 1.5 },
      { id: "DC-9999", qty: 1 },
    ]));
    expect(() => openPage(), "bad lines in the saved cart must not crash the page").not.toThrow();
    expect(linesOnScreen(), "only Golu × 2 is a good line: a known id and a whole-number qty above 0").toEqual([{ id: "DC-1042", qty: "2" }]);
    expect(totalText(), "the total should count only the good line: ₹900.00").toBe("₹900.00");
  });
});
