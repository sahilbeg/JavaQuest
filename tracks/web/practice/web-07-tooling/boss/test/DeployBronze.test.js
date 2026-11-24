import { describe, it, expect } from "vitest";
import { resolveImport } from "../src/imports.js";

const serverFiles = ["index.html", "src/main.js", "src/cart.js", "src/lib/money.js", "public/images/golu.png"];

/** The message of the error fn throws, or null when it does not throw. */
function errorMessage(fn) {
  try {
    fn();
    return null;
  } catch (error) {
    return error.message;
  }
}

describe("resolveImport, as strict as the Linux server", () => {
  it("finds a file whose name matches exactly", () => {
    expect(resolveImport("src/main.js", "./cart.js", serverFiles), "./cart.js from src/main.js is src/cart.js").toBe(
      "src/cart.js",
    );
  });

  it("follows ../ out of a folder", () => {
    expect(
      resolveImport("src/components/CartDrawer.js", "../lib/money.js", serverFiles),
      "../lib/money.js from src/components/ is src/lib/money.js",
    ).toBe("src/lib/money.js");
  });

  it("refuses ./Cart.js when the file is cart.js, like the deploy server", () => {
    const message = errorMessage(() => resolveImport("src/main.js", "./Cart.js", serverFiles));
    expect(message, "./Cart.js must not match src/cart.js: Linux file names are case-sensitive").not.toBeNull();
    expect(message ?? "", "the error message should start with Cannot find").toMatch(/^Cannot find/);
  });

  it("suggests the file with the right capitals", () => {
    const message = errorMessage(() => resolveImport("src/main.js", "./Cart.js", serverFiles));
    expect(message ?? "", "the message should say: did you mean src/cart.js?").toContain("did you mean src/cart.js?");
  });

  it("picks the exact file when two names differ only in capitals", () => {
    const files = ["src/main.js", "src/cart.js", "src/Cart.js"];
    expect(resolveImport("src/main.js", "./Cart.js", files), "./Cart.js should find src/Cart.js, not src/cart.js").toBe(
      "src/Cart.js",
    );
  });

  it("does not suggest anything when no file is even close", () => {
    const message = errorMessage(() => resolveImport("src/main.js", "./checkout.js", serverFiles));
    expect(message ?? "", "a missing file should still throw Cannot find").toMatch(/^Cannot find/);
    expect(message ?? "", "with no near match, there is nothing to suggest").not.toContain("did you mean");
  });
});
