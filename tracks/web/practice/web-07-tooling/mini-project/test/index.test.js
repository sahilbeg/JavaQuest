import { describe, it, expect } from "vitest";
import * as toolbox from "../src/index.js";
import { formatRupees, paiseFromRupees } from "../src/money.js";
import { slugify } from "../src/text.js";
import { isPunePin } from "../src/pin.js";

describe("the toolbox's front door, src/index.js", () => {
  it("exports exactly the four tools", () => {
    expect(Object.keys(toolbox).sort(), "index.js should export formatRupees, isPunePin, paiseFromRupees and slugify, and nothing else").toEqual([
      "formatRupees",
      "isPunePin",
      "paiseFromRupees",
      "slugify",
    ]);
  });

  it("passes on the very same functions as the files behind it", () => {
    expect(toolbox.formatRupees, "formatRupees should come from money.js").toBe(formatRupees);
    expect(toolbox.paiseFromRupees, "paiseFromRupees should come from money.js").toBe(paiseFromRupees);
    expect(toolbox.slugify, "slugify should come from text.js").toBe(slugify);
    expect(toolbox.isPunePin, "isPunePin should come from pin.js").toBe(isPunePin);
  });

  it("round-trips a price a customer typed", () => {
    const paise = toolbox.paiseFromRupees?.("₹1,299.50");
    expect(toolbox.formatRupees(paise ?? 0), "₹1,299.50 typed in should be shown back as ₹1,299.50").toBe("₹1,299.50");
  });
});
