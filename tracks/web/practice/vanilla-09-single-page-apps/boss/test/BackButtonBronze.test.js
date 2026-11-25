import { describe, it, expect, afterEach } from "vitest";
import { freshShop, browserMovesTo } from "./shop.js";

let shop;

afterEach(() => {
  shop?.router.stop();
});

describe("Bronze: Back and Forward show the right page", () => {
  it("shows the catalogue again after Back from an item", () => {
    shop = freshShop("/");
    shop.router.start();
    shop.router.navigate("/items");
    shop.router.navigate("/items/DC-1042");
    browserMovesTo("/items");
    expect(shop.screen(), "Back from Golu's page leads to /items, so the catalogue should show").toBe("Catalogue");
  });

  it("reaches the home page after Back twice", () => {
    shop = freshShop("/");
    shop.router.start();
    shop.router.navigate("/items");
    shop.router.navigate("/items/DC-1042");
    browserMovesTo("/items");
    browserMovesTo("/");
    expect(shop.screen(), "two presses of Back lead to /, so the home page should show").toBe("Home");
  });

  it("follows Forward as well", () => {
    shop = freshShop("/");
    shop.router.start();
    shop.router.navigate("/cart");
    browserMovesTo("/");
    browserMovesTo("/cart");
    expect(shop.screen(), "Forward to /cart should show the cart").toBe("Cart");
  });

  it("goes back to the page a deep link opened", () => {
    shop = freshShop("/cart");
    shop.router.start();
    expect(shop.screen(), "a link to /cart should open the cart").toBe("Cart");
    shop.router.navigate("/items");
    browserMovesTo("/cart");
    expect(shop.screen(), "Back from the catalogue should return to the cart the customer opened").toBe("Cart");
  });
});
