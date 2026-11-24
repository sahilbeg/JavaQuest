import { describe, it, expect } from "vitest";
import { sumPaise, countAbove, toysFromYarn, firstIndexOver } from "../src/cart.js";

describe("Basket loops", () => {
  it("adds up every price in the basket", () => {
    expect(sumPaise([15000, 12000, 45000]), "a coaster set, a keychain and an amigurumi add up to 72000 paise").toBe(72000);
    expect(sumPaise([89900, 89900]), "two tote bags add up to 179800 paise").toBe(179800);
    expect(sumPaise([]), "an empty basket adds up to 0 paise").toBe(0);
  });

  it("counts the prices above a limit", () => {
    expect(
      countAbove([15000, 12000, 45000, 89900, 35000], 20000),
      "three of these prices are above 20000 paise: 45000, 89900 and 35000",
    ).toBe(3);
    expect(countAbove([15000], 15000), "a price equal to the limit is not above it").toBe(0);
    expect(countAbove([], 0), "an empty basket has nothing above any limit").toBe(0);
  });

  it("counts how many 40 g toys the yarn makes", () => {
    expect(toysFromYarn(200), "200 g makes 5 toys of 40 g").toBe(5);
    expect(toysFromYarn(130), "130 g makes 3 whole toys, with 10 g left over").toBe(3);
    expect(toysFromYarn(85), "85 g makes 2 whole toys").toBe(2);
    expect(toysFromYarn(39), "39 g is not enough for even one toy").toBe(0);
    expect(toysFromYarn(0), "no yarn makes no toys").toBe(0);
  });

  it("finds the position of the first price above a limit", () => {
    expect(firstIndexOver([15000, 12000, 45000, 89900], 40000), "45000 at position 2 is the first price above 40000").toBe(2);
    expect(firstIndexOver([89900, 12000], 0), "the very first price, at position 0, is above 0").toBe(0);
    expect(firstIndexOver([15000, 12000], 40000), "no price is above 40000, so the answer is -1").toBe(-1);
  });
});
