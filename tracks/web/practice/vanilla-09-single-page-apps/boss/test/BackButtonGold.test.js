import { describe, it, expect, afterEach, vi } from "vitest";
import { freshShop, browserMovesTo } from "./shop.js";

let shop;

afterEach(() => {
  shop?.router.stop();
  vi.restoreAllMocks();
});

describe("Gold: the title and the filter follow the address", () => {
  it("sets the title when the app opens", () => {
    shop = freshShop("/cart");
    shop.router.start();
    expect(document.title, "opening /cart should set the title to \"Cart | Dhaaga & Co.\"").toBe("Cart | Dhaaga & Co.");
  });

  it("sets the title when the customer taps a link", () => {
    shop = freshShop("/");
    shop.router.start();
    shop.router.navigate("/items/DC-1042");
    expect(document.title, "Golu's page should be titled \"Item DC-1042 | Dhaaga & Co.\"").toBe("Item DC-1042 | Dhaaga & Co.");
  });

  it("sets the title on Back", () => {
    shop = freshShop("/");
    shop.router.start();
    shop.router.navigate("/items");
    shop.router.navigate("/items/DC-1042");
    browserMovesTo("/items");
    expect(document.title, "after Back to /items, the tab should say \"Catalogue | Dhaaga & Co.\"").toBe("Catalogue | Dhaaga & Co.");
  });

  it("titles the not found page", () => {
    shop = freshShop("/");
    shop.router.start();
    shop.router.navigate("/hats");
    expect(document.title, "an unknown address should be titled \"Page not found | Dhaaga & Co.\"").toBe("Page not found | Dhaaga & Co.");
  });

  it("keeps the colour filter when the customer comes Back", () => {
    shop = freshShop("/");
    shop.router.start();
    shop.router.navigate("/items?colour=Indigo");
    expect(shop.screen(), "the catalogue should get the colour from ?colour=Indigo").toBe("Catalogue: Indigo");
    shop.router.navigate("/items/DC-1042");
    browserMovesTo("/items?colour=Indigo");
    expect(shop.screen(), "Back should bring the Indigo filter back too, from location.search").toBe("Catalogue: Indigo");
  });

  it("treats a different filter as a new page", () => {
    shop = freshShop("/");
    shop.router.start();
    const push = vi.spyOn(history, "pushState");
    shop.router.navigate("/items?colour=Indigo");
    shop.router.navigate("/items?colour=Mint");
    expect(push.mock.calls.length, "Indigo then Mint are two different addresses, so both get an entry").toBe(2);
    expect(shop.screen(), "the Mint filter should show").toBe("Catalogue: Mint");
  });
});
