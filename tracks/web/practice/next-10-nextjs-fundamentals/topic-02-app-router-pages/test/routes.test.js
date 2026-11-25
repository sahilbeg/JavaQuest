import { describe, it, expect } from "vitest";
import { routeFor } from "../src/lib/routes.js";

describe("routeFor", () => {
  it("gives the page directly in app the address /", () => {
    expect(routeFor("app/page.jsx"), "app/page.jsx is the home page, so it should be \"/\"").toBe("/");
  });

  it("turns each folder into one part of the address", () => {
    expect(routeFor("app/visit/page.jsx"), "app/visit/page.jsx should be \"/visit\"").toBe("/visit");
    expect(routeFor("app/help/delivery/page.js"), "app/help/delivery/page.js should be \"/help/delivery\"").toBe("/help/delivery");
  });

  it("keeps a dynamic folder like [id] as it is", () => {
    expect(routeFor("app/items/[id]/page.jsx"), "app/items/[id]/page.jsx should be \"/items/[id]\"").toBe("/items/[id]");
  });

  it("returns null for files that are not called page", () => {
    expect(routeFor("app/about/layout.jsx"), "a layout is not a page, so app/about/layout.jsx should be null").toBeNull();
    expect(routeFor("app/visit/Hours.jsx"), "a component next to a page is not a page, so app/visit/Hours.jsx should be null").toBeNull();
  });

  it("leaves route groups in brackets out of the address", () => {
    expect(routeFor("app/(shop)/cart/page.jsx"), "the group (shop) is left out, so app/(shop)/cart/page.jsx should be \"/cart\"").toBe("/cart");
  });

  it("returns null for anything inside a private folder", () => {
    expect(routeFor("app/_components/page.jsx"), "folders starting with _ are private, so app/_components/page.jsx should be null").toBeNull();
    expect(
      routeFor("app/shop/_drafts/new/page.jsx"),
      "a private folder anywhere in the path hides the page, so app/shop/_drafts/new/page.jsx should be null",
    ).toBeNull();
  });
});
