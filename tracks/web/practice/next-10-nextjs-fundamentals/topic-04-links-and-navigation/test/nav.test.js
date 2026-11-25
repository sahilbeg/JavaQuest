import { describe, it, expect } from "vitest";
import { isActive } from "../src/lib/nav.js";

describe("isActive", () => {
  it("marks Home as active on the home page", () => {
    expect(isActive("/", "/"), "on \"/\", the Home link \"/\" should be active").toBe(true);
  });

  it("never marks Home as active on other pages", () => {
    expect(isActive("/items", "/"), "on \"/items\", the Home link should not be active").toBe(false);
    expect(isActive("/help/delivery", "/"), "on \"/help/delivery\", the Home link should not be active").toBe(false);
  });

  it("marks a link as active on its own address", () => {
    expect(isActive("/items", "/items"), "on \"/items\", the Shop link \"/items\" should be active").toBe(true);
  });

  it("marks a link as active on the addresses inside it", () => {
    expect(isActive("/items/DC-1042", "/items"), "Golu's page \"/items/DC-1042\" is inside the shop, so \"/items\" should be active").toBe(true);
    expect(isActive("/help/delivery", "/help"), "\"/help/delivery\" is inside help, so \"/help\" should be active").toBe(true);
  });

  it("does not mark a link as active on an address that only starts with the same letters", () => {
    expect(isActive("/itemsale", "/items"), "\"/itemsale\" is not inside \"/items\", so the Shop link should not be active").toBe(false);
    expect(isActive("/items", "/help"), "on \"/items\", the Help link should not be active").toBe(false);
  });
});
