import { describe, it, expect, beforeEach } from "vitest";
import { savePin, loadPin, saveWishlist, loadWishlist, toggleWish, forgetMe } from "../src/prefs.js";

beforeEach(() => {
  localStorage.clear();
});

describe("savePin and loadPin", () => {
  it("saves the PIN under dhaaga:pin", () => {
    savePin("411038");
    expect(localStorage.getItem("dhaaga:pin"), "savePin should store \"411038\" under the key dhaaga:pin").toBe("411038");
  });

  it("reads back a saved PIN", () => {
    localStorage.setItem("dhaaga:pin", "411038");
    expect(loadPin(), "loadPin should return the PIN saved on an earlier visit").toBe("411038");
  });

  it("gives an empty string when no PIN is saved", () => {
    expect(loadPin(), "a first visit has no PIN, so loadPin should return \"\"").toBe("");
  });
});

describe("saveWishlist and loadWishlist", () => {
  it("saves the ids as JSON text", () => {
    saveWishlist(["DC-1042", "DC-1013"]);
    expect(localStorage.getItem("dhaaga:wishlist"), "the wishlist should be saved as the JSON text [\"DC-1042\",\"DC-1013\"]").toBe('["DC-1042","DC-1013"]');
  });

  it("reads back a saved wishlist as an array", () => {
    localStorage.setItem("dhaaga:wishlist", '["DC-1042","DC-1013"]');
    expect(loadWishlist(), "loadWishlist should turn the saved text back into the array").toEqual(["DC-1042", "DC-1013"]);
  });

  it("gives an empty array when nothing is saved", () => {
    expect(loadWishlist(), "a first visit has no wishlist, so the answer is []").toEqual([]);
  });

  it("gives an empty array, without throwing, for broken text", () => {
    localStorage.setItem("dhaaga:wishlist", "[DC-1042");
    expect(() => loadWishlist(), "broken JSON must not crash the page: catch the error").not.toThrow();
    expect(loadWishlist(), "broken JSON should give []").toEqual([]);
  });

  it("gives an empty array when the saved JSON is not a list", () => {
    localStorage.setItem("dhaaga:wishlist", '{"DC-1042":true}');
    expect(loadWishlist(), "an object is valid JSON but not a list, so the answer is []").toEqual([]);
  });
});

describe("toggleWish", () => {
  it("adds an id and saves it", () => {
    expect(toggleWish("DC-1042"), "the first heart should give [\"DC-1042\"]").toEqual(["DC-1042"]);
    expect(localStorage.getItem("dhaaga:wishlist"), "the new wishlist should be saved at once").toBe('["DC-1042"]');
  });

  it("adds new ids at the end", () => {
    toggleWish("DC-1042");
    expect(toggleWish("DC-1013"), "a second heart should be added after the first").toEqual(["DC-1042", "DC-1013"]);
  });

  it("removes an id that is already saved", () => {
    toggleWish("DC-1042");
    toggleWish("DC-1013");
    expect(toggleWish("DC-1042"), "tapping Golu's heart again should take Golu out").toEqual(["DC-1013"]);
    expect(localStorage.getItem("dhaaga:wishlist"), "the shorter wishlist should be saved").toBe('["DC-1013"]');
  });

  it("starts a fresh wishlist when the saved one is broken", () => {
    localStorage.setItem("dhaaga:wishlist", "oops");
    expect(toggleWish("DC-1042"), "with broken saved text, the heart should start a new list").toEqual(["DC-1042"]);
  });
});

describe("forgetMe", () => {
  it("forgets the PIN and the wishlist", () => {
    localStorage.setItem("dhaaga:pin", "411038");
    localStorage.setItem("dhaaga:wishlist", '["DC-1042"]');
    forgetMe();
    expect(localStorage.getItem("dhaaga:pin"), "the PIN should be gone").toBe(null);
    expect(localStorage.getItem("dhaaga:wishlist"), "the wishlist should be gone").toBe(null);
  });

  it("leaves other keys alone", () => {
    localStorage.setItem("dhaaga:pin", "411038");
    localStorage.setItem("dhaaga:colour", "Indigo");
    forgetMe();
    expect(localStorage.getItem("dhaaga:colour"), "forgetMe must not clear keys it doesn't own: use removeItem, not clear").toBe("Indigo");
  });
});
