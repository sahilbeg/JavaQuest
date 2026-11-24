import { describe, it, expect } from "vitest";
import { hasItem, countOf, addItem, codesLine } from "../src/basket.js";

describe("The WhatsApp basket", () => {
  it("knows whether a code is in the basket", () => {
    expect(hasItem(["CST", "KEY"], "KEY"), "KEY is in the basket").toBe(true);
    expect(hasItem(["CST", "KEY"], "BAG"), "BAG is not in the basket").toBe(false);
    expect(hasItem([], "CST"), "an empty basket has nothing in it").toBe(false);
  });

  it("counts every copy of a code, not just the first", () => {
    expect(countOf(["KEY", "CST", "KEY", "KEY"], "KEY"), "Mrs. D'Souza's basket has 3 keychains").toBe(3);
    expect(countOf(["KEY", "CST"], "CST"), "the basket has 1 coaster set").toBe(1);
    expect(countOf(["KEY", "CST"], "BAG"), "the basket has no tote bags").toBe(0);
    expect(countOf([], "KEY"), "an empty basket has 0 of everything").toBe(0);
  });

  it("adds a code to the end of the basket", () => {
    const basket = ["CST"];
    const result = addItem(basket, "KEY");
    expect(result, 'adding KEY to ["CST"] should give ["CST", "KEY"]').toEqual(["CST", "KEY"]);
    expect(basket, "addItem should change the basket it was given, with push").toEqual(["CST", "KEY"]);
    addItem(basket, "AMI");
    expect(basket, "a second add should put AMI at the end").toEqual(["CST", "KEY", "AMI"]);
  });

  it("writes the codes on one line", () => {
    expect(codesLine(["KEY", "CST", "KEY"]), 'the codes should be joined as "KEY, CST, KEY"').toBe("KEY, CST, KEY");
    expect(codesLine(["AMI"]), 'one code on its own is just "AMI"').toBe("AMI");
    expect(codesLine([]), 'an empty basket should say "Basket is empty"').toBe("Basket is empty");
  });
});
