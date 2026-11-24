import { describe, it, expect, beforeEach } from "vitest";
import { openPage, add, linesOnScreen, totalText, savedText } from "./page.js";

beforeEach(() => {
  localStorage.clear();
  openPage();
});

describe("Bronze: the cart is saved on every change", () => {
  it("saves Golu as soon as he is added", () => {
    add("DC-1042");
    expect(savedText(), "after adding Golu, dhaaga:cart should mention DC-1042: save after the change, not before").toContain("DC-1042");
  });

  it("saves the item that was added last", () => {
    add("DC-1042");
    add("DC-1013");
    expect(savedText(), "after adding Golu and then the keychain, dhaaga:cart should mention DC-1013 too").toContain("DC-1013");
  });

  it("still shows the cart on screen", () => {
    add("DC-1042");
    expect(linesOnScreen().map((line) => line.id), "the cart on screen should show Golu").toEqual(["DC-1042"]);
    expect(totalText(), "one Golu should total ₹450.00").toBe("₹450.00");
  });
});
