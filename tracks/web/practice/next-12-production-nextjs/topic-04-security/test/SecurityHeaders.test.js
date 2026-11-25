import { describe, it, expect } from "vitest";
import { securityHeaders } from "../src/lib/securityHeaders.js";

function header(key) {
  return securityHeaders.find((entry) => entry.key === key)?.value;
}

describe("securityHeaders", () => {
  it("stops browsers guessing file types", () => {
    expect(header("X-Content-Type-Options"), "X-Content-Type-Options should be nosniff (TODO 5)").toBe("nosniff");
  });

  it("sends only the site's address to other sites, never the full page address", () => {
    expect(header("Referrer-Policy"), "Referrer-Policy should be strict-origin-when-cross-origin (TODO 5)").toBe("strict-origin-when-cross-origin");
  });

  it("has a Content Security Policy that blocks framing, plugins and foreign scripts", () => {
    const policy = header("Content-Security-Policy");
    expect(typeof policy, "there should be a Content-Security-Policy header (TODO 5)").toBe("string");
    const directives = policy.split(";").map((part) => part.trim()).filter((part) => part !== "");
    for (const needed of ["default-src 'self'", "object-src 'none'", "base-uri 'self'", "form-action 'self'", "frame-ancestors 'none'"]) {
      expect(directives.includes(needed), `the policy should include ${needed} (TODO 5)`).toBe(true);
    }
    expect(policy.includes("unsafe-eval"), "a production policy should never allow 'unsafe-eval'").toBe(false);
  });
});
