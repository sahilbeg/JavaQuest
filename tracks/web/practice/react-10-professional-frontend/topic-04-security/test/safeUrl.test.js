import { describe, it, expect } from "vitest";
import { safeUrl } from "../src/safeUrl.js";

describe("safeUrl", () => {
  it("keeps full https and http addresses as they are", () => {
    expect(safeUrl("https://instagram.com/anjali.makes"), "an https address is safe to link").toBe("https://instagram.com/anjali.makes");
    expect(safeUrl("http://dhaaga.in/reviews"), "an http address is safe to link").toBe("http://dhaaga.in/reviews");
  });

  it("refuses javascript: links, however they are written", () => {
    expect(safeUrl("javascript:alert(document.cookie)"), "a javascript: link runs code when clicked, so it must give null").toBe(null);
    expect(safeUrl("  JavaScript:alert(1)"), "capitals and leading spaces still make a javascript: link, so it must give null").toBe(null);
  });

  it("refuses every other kind of address", () => {
    expect(safeUrl("data:text/html,<script>alert(1)</script>"), "a data: address can carry a whole page, so it must give null").toBe(null);
    expect(safeUrl("file:///etc/passwd"), "a file: address must give null").toBe(null);
  });

  it("refuses things that aren't full addresses", () => {
    expect(safeUrl("instagram.com/anjali.makes"), "an address with no https:// can't be parsed alone, so it gives null").toBe(null);
    expect(safeUrl(""), "an empty website gives null").toBe(null);
    expect(safeUrl(undefined), "a review with no website gives null").toBe(null);
  });
});
