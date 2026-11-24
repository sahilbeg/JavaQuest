import { describe, it, expect, beforeEach } from "vitest";
import { openPage, add, linesOnScreen, totalText, emptyMessageShown } from "./page.js";

beforeEach(() => {
  localStorage.clear();
});

describe("Silver: the cart loads on start, safely", () => {
  it("brings the cart back after a refresh", () => {
    openPage();
    add("DC-1042");
    add("DC-1013");
    openPage();
    expect(linesOnScreen().map((line) => line.id), "after a refresh, Golu and the keychain should still be in the cart: load from dhaaga:cart").toEqual(["DC-1042", "DC-1013"]);
    expect(emptyMessageShown(), "a cart with items should hide the empty message").toBe(false);
  });

  it("shows the empty cart on a first visit", () => {
    openPage();
    expect(linesOnScreen().length, "a first visit has no saved cart, so no lines").toBe(0);
    expect(emptyMessageShown(), "a first visit should show Your cart is empty.").toBe(true);
    expect(totalText(), "an empty cart totals ₹0.00").toBe("₹0.00");
  });

  it("shows the empty cart instead of crashing on broken saved text", () => {
    localStorage.setItem("dhaaga:cart", "{oops");
    expect(() => openPage(), "broken saved text must not crash the page: catch the JSON.parse error").not.toThrow();
    expect(emptyMessageShown(), "with broken saved text, the cart should start empty").toBe(true);
  });

  it("saves a good cart again after a broken one", () => {
    localStorage.setItem("dhaaga:cart", "{oops");
    try {
      openPage();
    } catch {
      // the check above reports the crash; carry on to see if saving works
    }
    add("DC-1001");
    openPage();
    expect(linesOnScreen().map((line) => line.id), "after the broken value, adding the coaster set should save a cart that comes back").toEqual(["DC-1001"]);
  });
});
