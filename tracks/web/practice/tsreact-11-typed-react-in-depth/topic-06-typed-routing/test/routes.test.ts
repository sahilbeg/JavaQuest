import { describe, it, expect } from "vitest";
import { parseRoute, toHash } from "../src/routes.ts";

describe("parseRoute", () => {
  it("sends / to the shop", () => {
    expect(parseRoute("/"), "the path / should be { page: \"shop\" }").toEqual({ page: "shop" });
  });

  it("reads an item's id", () => {
    expect(parseRoute("/item/DC-1042"), "/item/DC-1042 should be the item page for DC-1042").toEqual({ page: "item", id: "DC-1042" });
  });

  it("reads an order's id", () => {
    expect(parseRoute("/order/order-17"), "/order/order-17 should be the order page for order-17").toEqual({
      page: "order",
      orderId: "order-17",
    });
  });

  it("calls an item with no id not found", () => {
    expect(parseRoute("/item/"), "/item/ has an empty id, so it should be notFound").toEqual({ page: "notFound", path: "/item/" });
  });

  it("calls an address with extra parts not found", () => {
    expect(parseRoute("/item/DC-1042/photos"), "/item/DC-1042/photos has one part too many").toEqual({
      page: "notFound",
      path: "/item/DC-1042/photos",
    });
  });

  it("calls an unknown section not found", () => {
    expect(parseRoute("/about"), "/about isn't a page of the store").toEqual({ page: "notFound", path: "/about" });
    expect(parseRoute("/basket/DC-1042"), "basket isn't a section the store knows").toEqual({ page: "notFound", path: "/basket/DC-1042" });
  });
});

describe("toHash", () => {
  it("builds the hash for each kind of page", () => {
    expect(toHash({ page: "shop" }), "the shop's hash should be #/").toBe("#/");
    expect(toHash({ page: "item", id: "DC-2001" }), "an item's hash should be #/item/DC-2001").toBe("#/item/DC-2001");
    expect(toHash({ page: "order", orderId: "order-18" }), "an order's hash should be #/order/order-18").toBe("#/order/order-18");
    expect(toHash({ page: "notFound", path: "/about" }), "a notFound route should go back to #/about").toBe("#/about");
  });

  it("is the opposite of parseRoute", () => {
    const route = { page: "item", id: "DC-1042" };
    expect(parseRoute(toHash(route).slice(1)), "parsing the hash a route builds should give back the same route").toEqual(route);
  });
});
