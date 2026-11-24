import { describe, it, expect } from "vitest";
import { value } from "./css.js";

describe("style.css", () => {
  it("gives a picked chip an Indigo border", () => {
    expect(value(".chip--selected", "border-color"), ".chip--selected should set border-color: #2E3A8C").toBe("#2e3a8c");
  });

  it("highlights a sale price in Marigold", () => {
    expect(value(".price--sale", "background-color"), ".price--sale should set background-color: #F2A81D").toBe("#f2a81d");
  });
});
