import { describe, it, expect } from "vitest";
import { page, source } from "./html.js";

describe("Silver: the charset line comes first", () => {
  it("puts the charset line first inside the head", () => {
    const first = page().head.firstElementChild;
    const isCharset = first !== null && first.tagName.toLowerCase() === "meta" && first.hasAttribute("charset");
    expect(isCharset, 'the very first element inside <head> should be <meta charset="utf-8">, before the <title> and the <style>').toBe(true);
  });

  it("puts the charset line within the first 1024 bytes of the file", () => {
    const text = source();
    const at = text.search(/<meta\s+charset/i);
    expect(at, 'the file should contain <meta charset="utf-8">').toBeGreaterThanOrEqual(0);
    const bytesBefore = new TextEncoder().encode(text.slice(0, at)).length;
    expect(bytesBefore, `the charset line starts after ${bytesBefore} bytes, but browsers only look in the first 1024`).toBeLessThan(1024);
  });
});
