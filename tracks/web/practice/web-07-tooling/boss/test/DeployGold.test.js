import { describe, it, expect } from "vitest";
import { assetUrl } from "../src/assets.js";

describe("assetUrl", () => {
  it("joins the base and a plain path", () => {
    expect(assetUrl("/", "images/golu.png"), "base / and images/golu.png").toBe("/images/golu.png");
    expect(assetUrl("/shop/", "images/golu.png"), "base /shop/ and images/golu.png").toBe("/shop/images/golu.png");
  });

  it("turns every Windows backslash into a slash", () => {
    expect(
      assetUrl("/shop/", "images\\diwali\\banner.png"),
      "images\\diwali\\banner.png should become images/diwali/banner.png",
    ).toBe("/shop/images/diwali/banner.png");
  });

  it("never makes a double slash from a leading slash", () => {
    expect(assetUrl("/", "/images/golu.png"), "//images/golu.png would point at a site called images").toBe(
      "/images/golu.png",
    );
  });

  it("adds the missing slash after the base and drops ./", () => {
    expect(assetUrl("/shop", "./images/golu.png"), "base /shop and ./images/golu.png").toBe("/shop/images/golu.png");
  });

  it("refuses paths that only exist on one computer", () => {
    for (const path of [
      "C:\\Users\\sana\\dhaaga\\public\\golu.png",
      "D:/dhaaga/public/golu.png",
      "/Users/kavya/dhaaga/public/golu.png",
      "/home/arjun/dhaaga/public/golu.png",
      "file:///Users/kavya/golu.png",
    ]) {
      expect(() => assetUrl("/", path), `${path} should throw an Error mentioning one computer`).toThrow(/one computer/);
    }
  });
});
