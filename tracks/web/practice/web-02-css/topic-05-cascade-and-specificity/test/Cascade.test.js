import { describe, it, expect } from "vitest";
import { rules, value } from "./css.js";

describe("Which rule wins on the sold-out card", () => {
  it("lets the red Sold out label beat the grey paragraph rule", () => {
    expect(value("#catalogue p", "color"), "the id selector #catalogue p should be gone: change it to main p").toBeUndefined();
    expect(value("main p", "color"), "main p should keep color: #5a5a5a").toBe("#5a5a5a");
    expect(value(".stock", "color"), ".stock should stay red, #b3261e").toBe("#b3261e");
  });

  it("uses no id selectors to set colours", () => {
    const idRules = rules().filter((rule) => rule.selector.includes("#") && rule.decls.has("color"));
    expect(idRules.map((rule) => rule.selector), "an id selector beats every class, so no rule with # should set color").toEqual([]);
  });

  it("uses no !important anywhere", () => {
    const shouting = rules().filter((rule) => [...rule.decls.values()].some((v) => v.includes("!important")));
    expect(shouting.map((rule) => rule.selector), "delete !important and let specificity decide").toEqual([]);
    expect(value("h3", "color"), "h3 should keep color: #2e3a8c, without !important").toBe("#2e3a8c");
  });

  it("greys out a sold-out item's name", () => {
    expect(value(".sold-out h3", "color"), ".sold-out h3 should have color: #5a5a5a").toBe("#5a5a5a");
  });

  it("keeps every Order button Marigold", () => {
    expect(value(".buy", "background-color"), "the last .buy rule should set #f2a81d: delete the old gray one").toBe("#f2a81d");
  });

  it("strikes through a sold-out item's price", () => {
    expect(value(".sold-out .price", "text-decoration"), ".sold-out .price should have text-decoration: line-through").toBe("line-through");
  });
});
