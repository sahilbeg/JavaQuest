import { describe, it, expect } from "vitest";
import { isBundle, priceOf, bundlesOnly, isPresent, messageFor } from "../src/listings.ts";
import type { Listing } from "../src/listings.ts";

const golu: Listing = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };
const keychain: Listing = { id: "DC-1044", name: "Mint keychain", paise: 12000 };
const diwaliBox: Listing = { id: "GB-01", name: "Diwali gift box", itemIds: ["DC-1042", "DC-1043"], bundlePaise: 55000 };
const babyBox: Listing = { id: "GB-02", name: "Baby shower set", itemIds: ["DC-1047", "DC-1044"], bundlePaise: 42000 };
const priceList: Listing[] = [golu, diwaliBox, keychain, babyBox];

describe("isBundle", () => {
  it("says true for a gift bundle", () => {
    expect(isBundle(diwaliBox), "the Diwali gift box has itemIds, so it is a bundle").toBe(true);
  });

  it("says false for a single product", () => {
    expect(isBundle(golu), "Golu has no itemIds, so he is a product, not a bundle").toBe(false);
  });
});

describe("priceOf", () => {
  it("gives a product's paise", () => {
    expect(priceOf(golu), "Golu costs 45000 paise").toBe(45000);
  });

  it("gives a bundle's bundlePaise", () => {
    expect(priceOf(diwaliBox), "the Diwali gift box costs 55000 paise, from bundlePaise").toBe(55000);
  });
});

describe("bundlesOnly", () => {
  it("keeps only the bundles, in order", () => {
    expect(bundlesOnly(priceList).map((bundle) => bundle.id), "the price list has two bundles: GB-01, then GB-02").toEqual(["GB-01", "GB-02"]);
  });
});

describe("isPresent", () => {
  it("drops null and undefined from a list", () => {
    const ids = ["DC-1042", null, "DC-1044", undefined].filter(isPresent);
    expect(ids, "only the two real ids should be left").toEqual(["DC-1042", "DC-1044"]);
  });

  it("keeps 0 and the empty string, because they are real values", () => {
    expect(isPresent(0), "a stock count of 0 is a real value").toBe(true);
    expect(isPresent(""), "an empty note is still a value").toBe(true);
  });

  it("says false for null and undefined", () => {
    expect(isPresent(null), "null is not present").toBe(false);
    expect(isPresent(undefined), "undefined is not present").toBe(false);
  });
});

describe("messageFor", () => {
  it("gives an Error's message", () => {
    expect(messageFor(new Error("Out of stock")), "an Error should show its own message").toBe("Out of stock");
  });

  it("works for kinds of Error too", () => {
    expect(messageFor(new TypeError("Failed to fetch")), "a TypeError is an Error too").toBe("Failed to fetch");
  });

  it("shows a thrown string as it is", () => {
    expect(messageFor("UPI timed out"), "a plain string should be shown as it is").toBe("UPI timed out");
  });

  it("gives a general message for anything else", () => {
    expect(messageFor(404), "a number thrown by mistake should give the general message").toBe("Something went wrong");
    expect(messageFor(undefined), "undefined should give the general message").toBe("Something went wrong");
  });
});
