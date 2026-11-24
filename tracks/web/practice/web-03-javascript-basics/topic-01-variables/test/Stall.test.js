import { describe, it, expect } from "vitest";
import { SHOP_NAME, CITY, coastersAfterSunday, welcomeLine } from "../src/stall.js";

describe("The stall counter", () => {
  it("remembers the shop's name in SHOP_NAME", () => {
    expect(SHOP_NAME, 'SHOP_NAME should hold the text "Dhaaga & Co."').toBe("Dhaaga & Co.");
  });

  it("remembers the city in CITY", () => {
    expect(CITY, 'CITY should hold the text "Pune"').toBe("Pune");
  });

  it("counts the coaster sets left after Sunday", () => {
    expect(coastersAfterSunday(), "12 - 3 + 5 - 4 leaves 10 coaster sets in the box").toBe(10);
  });

  it("welcomes a customer by name", () => {
    expect(welcomeLine("Asha"), 'welcomeLine("Asha") should be "Welcome to Dhaaga & Co., Asha!"').toBe(
      "Welcome to Dhaaga & Co., Asha!",
    );
    expect(welcomeLine("Mr. Joshi"), 'welcomeLine("Mr. Joshi") should be "Welcome to Dhaaga & Co., Mr. Joshi!"').toBe(
      "Welcome to Dhaaga & Co., Mr. Joshi!",
    );
  });
});
