import { describe, it, expect } from "vitest";
import { matchRoute } from "../src/router.js";

describe("matchRoute", () => {
  it("sends / to the home page", () => {
    expect(matchRoute("/"), "\"/\" should be { page: \"home\" }").toEqual({ page: "home" });
  });

  it("sends /cart to the cart page", () => {
    expect(matchRoute("/cart"), "\"/cart\" should be { page: \"cart\" }").toEqual({ page: "cart" });
  });

  it("sends /item/DC-1042 to the item page with its id", () => {
    expect(matchRoute("/item/DC-1042"), "\"/item/DC-1042\" should be { page: \"item\", id: \"DC-1042\" }").toEqual({
      page: "item",
      id: "DC-1042",
    });
  });

  it("treats /item/ with no id as not found", () => {
    expect(matchRoute("/item/"), "\"/item/\" has no id, so it should be { page: \"not-found\" }").toEqual({ page: "not-found" });
  });

  it("treats any other address as not found", () => {
    expect(matchRoute("/about"), "\"/about\" should be { page: \"not-found\" }").toEqual({ page: "not-found" });
    expect(matchRoute("/cart/extra"), "\"/cart/extra\" should be { page: \"not-found\" }").toEqual({ page: "not-found" });
  });
});
