import { describe, it, expect } from "vitest";
import { value } from "./css.js";

describe("The product grid", () => {
  it("turns the products list into a grid container", () => {
    expect(value(".products", "display"), ".products should have display: grid instead of flex").toBe("grid");
    expect(value(".products", "flex-wrap"), "delete flex-wrap from .products: grids don't wrap like flex rows").toBeUndefined();
  });

  it("draws three equal columns", () => {
    expect(value(".products", "grid-template-columns") ?? "", ".products should have grid-template-columns: repeat(3, 1fr)").toMatch(/^(repeat\(\s*3\s*,\s*1fr\s*\)|1fr 1fr 1fr)$/);
  });

  it("leaves 1.5rem between the cards", () => {
    expect(value(".products", "gap"), ".products should have gap: 1.5rem").toBe("1.5rem");
  });

  it("makes Golu's featured card two columns wide", () => {
    expect(value(".featured", "grid-column"), ".featured should have grid-column: span 2").toBe("span 2");
  });
});
