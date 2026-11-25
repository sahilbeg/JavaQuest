// These checks test the finished app in src/app.js, using YOUR test kit. They pass once the kit works.
import { describe, it, expect, beforeEach, afterEach } from "vitest";
import { startApp } from "../src/app.js";
import { visit, fakeServer, waitFor, textOf, reset } from "../src/testkit.js";

const catalogue = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-2001", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-3001", name: "Indigo tote bag", paise: 89900 },
];

let stop = () => {};
let server;

beforeEach(() => {
  reset();
  document.body.innerHTML = `<main id="app"></main>`;
  server = fakeServer({ "/api/items": catalogue, "/api/items/DC-1042": catalogue[0] });
  stop = startApp(document.querySelector("#app"), { fetch: server.fetch });
});

afterEach(() => {
  stop();
  stop = () => {};
});

const root = () => document.querySelector("#app");
const linkNames = () => [...root().querySelectorAll("a")].map((a) => textOf(a));
const heading = () => {
  const h1 = root().querySelector("h1");
  return h1 === null ? "" : textOf(h1);
};

describe("The store, tested with your kit", () => {
  it("shows the catalogue from the API at /items", async () => {
    visit("/items");
    await waitFor(() => heading() === "Catalogue");
    expect(linkNames(), "the catalogue should list the three items from the pretend server").toEqual([
      "Golu the elephant",
      "Marigold coaster set",
      "Indigo tote bag",
    ]);
    expect(server.calls, "the catalogue should ask the API for /api/items once").toEqual(["/api/items"]);
  });

  it("opens an item when its link is tapped", async () => {
    visit("/items");
    const link = await waitFor(() => root().querySelector('a[href="/items/DC-1042"]'));
    link?.click();
    await waitFor(() => heading() === "Golu the elephant");
    expect(location.pathname, "tapping Golu should change the address").toBe("/items/DC-1042");
    expect(textOf(root()).includes("₹450.00"), "Golu's page should show his price, ₹450.00").toBe(true);
    expect(server.calls, "the item page should ask for /api/items/DC-1042").toEqual(["/api/items", "/api/items/DC-1042"]);
  });

  it("shows not found for an item the API doesn't know", async () => {
    visit("/items/DC-9999");
    await waitFor(() => heading() === "Item not found");
    expect(heading(), "the pretend server answers 404, so the page should say Item not found").toBe("Item not found");
  });

  it("goes back to the catalogue on Back", async () => {
    visit("/items");
    const link = await waitFor(() => root().querySelector('a[href="/items/DC-1042"]'));
    link?.click();
    await waitFor(() => heading() === "Golu the elephant");
    visit("/items");
    await waitFor(() => heading() === "Catalogue");
    expect(linkNames().length, "after Back, the catalogue's three links should be there again").toBe(3);
  });
});
