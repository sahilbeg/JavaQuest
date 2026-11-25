import { describe, it, expect } from "vitest";
import sitemap from "../src/app/sitemap.js";
import robots from "../src/app/robots.js";

describe("robots.txt", () => {
  it("lets search engines in, keeps them out of the cart and checkout, and names the sitemap", () => {
    expect(robots(), "robots() should allow \"/\", disallow [\"/cart\", \"/checkout\"] and point to https://dhaaga.in/sitemap.xml (TODO 4)").toEqual({
      rules: { userAgent: "*", allow: "/", disallow: ["/cart", "/checkout"] },
      sitemap: "https://dhaaga.in/sitemap.xml",
    });
  });
});

describe("sitemap.xml", () => {
  it("lists the home page and every item page, with full addresses", async () => {
    const entries = await sitemap();
    expect(Array.isArray(entries), "sitemap() should return an array").toBe(true);
    expect(entries.map((entry) => entry.url), "the sitemap should list https://dhaaga.in/ and then each item's page in catalogue order (TODO 4)").toEqual([
      "https://dhaaga.in/",
      "https://dhaaga.in/items/DC-1042",
      "https://dhaaga.in/items/DC-1101",
      "https://dhaaga.in/items/DC-1205",
      "https://dhaaga.in/items/DC-1301",
      "https://dhaaga.in/items/DC-1402",
    ]);
  });
});
