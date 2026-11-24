import { describe, it, expect, afterEach } from "vitest";
import { cleanup } from "@testing-library/react";
import { ProductCard } from "../src/ProductCard.jsx";
import { checkShowsPrice, checkAddsOne, checkAddsChosenQuantity, checkSoldOut } from "../src/productCardChecks.jsx";
import { ShowsPaise, SendsName, AddsTwice, IgnoresQuantity, UnlabelledQuantity, SoldOutStillSells } from "./brokenCards.jsx";

const CHECKS = { checkShowsPrice, checkAddsOne, checkAddsChosenQuantity, checkSoldOut };

afterEach(cleanup);

/** Runs one of your checks on a card. Returns "passed", or "failed: <first line of the error>". */
async function outcome(check, Card) {
  try {
    await check(Card);
    return "passed";
  } catch (error) {
    return "failed: " + String(error?.message ?? error).split("\n")[0];
  } finally {
    cleanup();
  }
}

/** The names of your checks that fail on this card. */
async function checksThatFail(Card) {
  const failing = [];
  for (const [name, check] of Object.entries(CHECKS)) {
    if ((await outcome(check, Card)) !== "passed") failing.push(name);
  }
  return failing;
}

describe("Your ProductCard checks", () => {
  it("all pass on the real, working ProductCard", async () => {
    for (const [name, check] of Object.entries(CHECKS)) {
      expect(await outcome(check, ProductCard), `${name} should pass on the real card. A check that fails on working code is wrong`).toBe("passed");
    }
  });

  it("catch a card that shows paise instead of rupees", async () => {
    expect((await checksThatFail(ShowsPaise)).length > 0, "no check failed on a card showing ₹45000.00. The worked example checkShowsPrice should catch this").toBe(true);
  });

  it("catch a card that sends the product's name instead of its id", async () => {
    expect((await checksThatFail(SendsName)).length > 0, "no check failed when onAdd got \"Golu the elephant\" instead of \"DC-1042\". Compare onAdd.mock.calls with the exact arguments (TODO 1)").toBe(true);
  });

  it("catch a card that adds the item twice on one click", async () => {
    expect((await checksThatFail(AddsTwice)).length > 0, "no check failed when one click called onAdd twice. Check the whole list of calls, not just the first one (TODO 1)").toBe(true);
  });

  it("catch a card that ignores the chosen quantity", async () => {
    expect((await checksThatFail(IgnoresQuantity)).length > 0, "no check failed when picking 3 still added 1. Pick 3, click, and expect [[\"DC-1042\", 3]] (TODO 2)").toBe(true);
  });

  it("catch a quantity box that has no label", async () => {
    expect((await checksThatFail(UnlabelledQuantity)).length > 0, "no check failed when the Quantity select lost its label. Find the select by its label, the way a screen reader user would (TODO 2)").toBe(true);
  });

  it("catch a sold-out card that still sells", async () => {
    expect((await checksThatFail(SoldOutStillSells)).length > 0, "no check failed when a sold-out card's button still worked. Check that it is disabled and that clicking adds nothing (TODO 3)").toBe(true);
  });
});
