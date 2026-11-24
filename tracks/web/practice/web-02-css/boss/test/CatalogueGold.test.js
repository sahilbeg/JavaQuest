import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

describe("Gold: photos fit, and nothing is hidden", () => {
  it("never lets a photo grow wider than its card", () => {
    const maxWidth = value(".product img", "max-width") ?? value("img", "max-width");
    expect(maxWidth, ".product img should have max-width: 100%").toBe("100%");
  });

  it("keeps each photo's shape as it shrinks", () => {
    const height = value(".product img", "height") ?? value("img", "height");
    expect(height, ".product img should have height: auto").toBe("auto");
  });

  it("stops hiding the overflow on the page", () => {
    const hiding = rules()
      .filter((rule) => rule.selector === "html" || rule.selector === "body")
      .filter((rule) => /hidden|clip/.test(rule.decls.get("overflow-x") ?? "") || /hidden|clip/.test(rule.decls.get("overflow") ?? ""))
      .map((rule) => rule.selector);
    expect(hiding, "delete overflow-x: hidden: it cuts off the Order buttons instead of making the page fit").toEqual([]);
  });
});
