import { describe, it, expect, beforeEach, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import RootLayout, { metadata } from "../src/app/layout.jsx";
import HomePage from "../src/app/page.jsx";
import { resetStore } from "../src/lib/store.js";

beforeEach(resetStore);
afterEach(cleanup);

describe("Stage 2: the layout", () => {
  it("sets the page language to Indian English", () => {
    const page = RootLayout({ children: <p>Hello</p> });
    expect(page.props.lang, "<html> should have lang=\"en-IN\" (TODO 2)").toBe("en-IN");
  });

  it("gives every page a title ending with the shop's name, a description and the site's address", () => {
    expect(metadata.title, "title should be { default: \"Dhaaga & Co.\", template: \"%s | Dhaaga & Co.\" } (TODO 2)").toEqual({
      default: "Dhaaga & Co.",
      template: "%s | Dhaaga & Co.",
    });
    expect(typeof metadata.description === "string" && metadata.description.length > 0, "metadata needs a description (TODO 2)").toBe(true);
    expect(String(metadata.metadataBase), "metadataBase should be new URL(\"https://dhaaga.in\") (TODO 2)").toBe("https://dhaaga.in/");
  });
});

describe("Stage 2: the catalogue page", () => {
  it("lists every item in catalogue order, each linking to its own page", async () => {
    render(await HomePage());
    expect(screen.queryByRole("heading", { level: 1, name: "Dhaaga & Co." }), "the page's h1 should be Dhaaga & Co.").not.toBeNull();
    const items = screen.queryAllByRole("listitem");
    expect(items.length, "there should be one <li> per item: 5 (TODO 3)").toBe(5);
    const links = items.map((item) => item.querySelector("a"));
    expect(links.map((link) => link?.textContent), "each <li> should start with a link named by the item, in catalogue order (TODO 3)").toEqual([
      "Golu the elephant",
      "Marigold coaster set",
      "Indigo keychain",
      "Tote bag",
      "Baby booties",
    ]);
    expect(links[0]?.getAttribute("href"), "Golu's link should go to /items/DC-1042 (TODO 3)").toBe("/items/DC-1042");
  });

  it("shows each price in rupees", async () => {
    render(await HomePage());
    const items = screen.queryAllByRole("listitem");
    expect(items[0]?.textContent.includes("₹450.00"), "Golu's line should show ₹450.00 (TODO 3)").toBe(true);
    expect(items[3]?.textContent.includes("₹899.00"), "the tote bag's line should show ₹899.00 (TODO 3)").toBe(true);
  });

  it("marks only the keychain as sold out", async () => {
    render(await HomePage());
    const soldOut = screen.queryAllByText("Sold out");
    expect(soldOut.length, "exactly one item, the Indigo keychain, has 0 in stock (TODO 3)").toBe(1);
    expect(soldOut[0].closest("li")?.textContent.includes("Indigo keychain"), "Sold out should be in the keychain's line (TODO 3)").toBe(true);
  });
});
