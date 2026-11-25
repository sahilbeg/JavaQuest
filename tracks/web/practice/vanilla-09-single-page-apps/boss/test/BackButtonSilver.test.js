import { describe, it, expect, afterEach, vi } from "vitest";
import { freshShop } from "./shop.js";

let shop;

afterEach(() => {
  shop?.router.stop();
  vi.restoreAllMocks();
});

describe("Silver: no extra history entries", () => {
  it("adds no entry when the customer taps the page she is already on", () => {
    shop = freshShop("/");
    shop.router.start();
    const push = vi.spyOn(history, "pushState");
    shop.router.navigate("/cart");
    shop.router.navigate("/cart");
    expect(push.mock.calls.length, "tapping Cart twice should add one history entry, not two").toBe(1);
    expect(shop.screen(), "the cart should still show").toBe("Cart");
  });

  it("sends an old /shop link to the catalogue with one entry", () => {
    shop = freshShop("/");
    shop.router.start();
    const push = vi.spyOn(history, "pushState");
    shop.router.navigate("/shop");
    expect(push.mock.calls.length, "/shop redirects to /items: only one entry should be added").toBe(1);
    expect(push.mock.calls[0]?.[2], "the one entry should be the new address, /items").toBe("/items");
    expect(location.pathname, "the address bar should say /items").toBe("/items");
    expect(shop.screen(), "the catalogue should show").toBe("Catalogue");
  });

  it("replaces an old address when the app opens on it", () => {
    shop = freshShop("/golu");
    const push = vi.spyOn(history, "pushState");
    const replace = vi.spyOn(history, "replaceState");
    shop.router.start();
    expect(location.pathname, "a printed /golu link should end up at /items/DC-1042").toBe("/items/DC-1042");
    expect(shop.screen(), "Golu's page should show").toBe("Item DC-1042");
    expect(replace.mock.calls.length, "the old address should be replaced with replaceState, so Back can't return to it").toBe(1);
    expect(push.mock.calls.length, "opening the app must not push an entry").toBe(0);
  });
});
