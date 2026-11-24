import { describe, it, expect } from "vitest";
import { badgeText, badgeLevel } from "../src/badges.js";

describe("badgeText", () => {
  it("says Sold out for 0", () => {
    expect(badgeText(0), "0 left should say \"Sold out\"").toBe("Sold out");
  });

  it("says how many are left for 1 to 3", () => {
    expect(badgeText(1), "1 left should say \"Only 1 left\"").toBe("Only 1 left");
    expect(badgeText(2), "2 left should say \"Only 2 left\"").toBe("Only 2 left");
    expect(badgeText(3), "3 left should say \"Only 3 left\"").toBe("Only 3 left");
  });

  it("says In stock for 4 or more", () => {
    expect(badgeText(4), "4 left is plenty, so \"In stock\"").toBe("In stock");
    expect(badgeText(12), "12 left should say \"In stock\"").toBe("In stock");
  });
});

describe("badgeLevel", () => {
  it("gives out, low or ok for Sana's colours", () => {
    expect(badgeLevel(0), "0 left is level \"out\"").toBe("out");
    expect(badgeLevel(1), "1 left is level \"low\"").toBe("low");
    expect(badgeLevel(3), "3 left is level \"low\"").toBe("low");
    expect(badgeLevel(4), "4 left is level \"ok\"").toBe("ok");
    expect(badgeLevel(40), "40 left is level \"ok\"").toBe("ok");
  });
});
