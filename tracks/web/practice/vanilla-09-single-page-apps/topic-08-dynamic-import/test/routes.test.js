import { describe, it, expect } from "vitest";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { routes } from "../src/routes.js";

/** src/routes.js as text, without its comments. Tests run with the ticket's folder as the working directory. */
function code() {
  return readFileSync(join(process.cwd(), "src", "routes.js"), "utf8")
    .replace(/\/\*[\s\S]*?\*\//g, "")
    .replace(/\/\/.*$/gm, "");
}

describe("routes.js", () => {
  it("doesn't import checkout.js at the top of the file", () => {
    const staticImport = /import\s[^;]*?from\s*["']\.\/checkout\.js["']/;
    expect(staticImport.test(code()), "a static import ... from \"./checkout.js\" puts the checkout in every customer's first download; remove it").toBe(false);
  });

  it("loads checkout.js with a dynamic import()", () => {
    const dynamicImport = /import\(\s*["']\.\/checkout\.js["']\s*\)/;
    expect(dynamicImport.test(code()), "the checkout route should load its code with import(\"./checkout.js\")").toBe(true);
  });

  it("gives the checkout route a load function that brings the checkout screen", async () => {
    const route = routes.find((r) => r.path === "/checkout");
    expect(typeof route?.load, "the /checkout route should have a load function").toBe("function");
    const module = await route.load();
    expect(typeof module?.default, "load() should give the checkout module, whose default export is the view").toBe("function");
    expect(module.default().querySelector("h1")?.textContent, "the checkout view should show its Checkout heading").toBe("Checkout");
  });

  it("keeps the catalogue as a normal view", () => {
    const route = routes.find((r) => r.path === "/items");
    expect(typeof route?.view, "the catalogue is opened by almost everyone, so it stays a normal view").toBe("function");
  });
});
