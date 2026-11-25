import { describe, it, expect, beforeEach, afterEach } from "vitest";
import { pathFromHash, matchOne, matchRoute, startHashRouter } from "../src/router.js";

function heading(text) {
  const h1 = document.createElement("h1");
  h1.textContent = text;
  return h1;
}

const routes = [
  { path: "/", view: () => heading("Home") },
  { path: "/items", view: () => heading("Catalogue") },
  { path: "/items/:id", view: ({ id }) => heading(`Item ${id}`) },
  { path: "/cart", view: () => heading("Cart") },
];
const notFound = (path) => heading(`Not found: ${path}`);

let outlet;
let stop = () => {};

beforeEach(() => {
  history.replaceState(null, "", "/");
  document.body.innerHTML = `<main id="app"></main>`;
  outlet = document.querySelector("#app");
});

afterEach(() => {
  stop();
  stop = () => {};
});

/** Changes the hash the way a tapped link would, and fires the hashchange event. */
function goTo(hash) {
  history.replaceState(null, "", hash);
  window.dispatchEvent(new Event("hashchange"));
}

const screen = () => outlet.textContent;

describe("pathFromHash", () => {
  it("drops the # sign from the hash", () => {
    expect(pathFromHash("#/items/DC-1042"), "\"#/items/DC-1042\" should give the path \"/items/DC-1042\"").toBe("/items/DC-1042");
    expect(pathFromHash("#/cart"), "\"#/cart\" should give \"/cart\"").toBe("/cart");
  });

  it("gives the home path when there is no hash", () => {
    expect(pathFromHash(""), "an empty hash means the home page, \"/\"").toBe("/");
    expect(pathFromHash("#"), "a lonely \"#\" also means the home page, \"/\"").toBe("/");
  });
});

describe("matchOne", () => {
  it("saves the :id part of the path as a param", () => {
    expect(matchOne("/items/:id", "/items/DC-1042"), "\"/items/:id\" should match \"/items/DC-1042\" with id \"DC-1042\"").toEqual({ id: "DC-1042" });
  });

  it("matches a fixed path with no params", () => {
    expect(matchOne("/cart", "/cart"), "\"/cart\" matches itself and has no params, so the answer is {}").toEqual({});
  });

  it("refuses paths with a different number of parts", () => {
    expect(matchOne("/items/:id", "/items/DC-1042/reviews"), "a longer path must not match").toBe(null);
    expect(matchOne("/items/:id", "/items"), "a shorter path must not match").toBe(null);
  });

  it("refuses a fixed part that is different", () => {
    expect(matchOne("/items/:id", "/hats/DC-1042"), "\"hats\" is not \"items\", so there is no match").toBe(null);
  });

  it("refuses an empty param", () => {
    expect(matchOne("/items/:id", "/items/"), "\"/items/\" has an empty id, so it must not match").toBe(null);
  });
});

describe("matchRoute", () => {
  it("finds the home route for /", () => {
    const match = matchRoute(routes, "/");
    expect(match?.route, "\"/\" should match the home route").toBe(routes[0]);
    expect(match?.params, "the home route has no params").toEqual({});
  });

  it("finds the item route and its id", () => {
    const match = matchRoute(routes, "/items/DC-1042");
    expect(match?.route, "\"/items/DC-1042\" should match the \"/items/:id\" route").toBe(routes[2]);
    expect(match?.params, "the id param should be \"DC-1042\"").toEqual({ id: "DC-1042" });
  });

  it("tells /items and /items/:id apart", () => {
    expect(matchRoute(routes, "/items")?.route, "\"/items\" is the catalogue, not an item").toBe(routes[1]);
    expect(matchRoute(routes, "/cart")?.route, "\"/cart\" should match the cart route").toBe(routes[3]);
  });

  it("gives null for an address no route knows", () => {
    expect(matchRoute(routes, "/hats"), "no route matches \"/hats\"").toBe(null);
  });

  it("uses the first route that matches", () => {
    const newItem = { path: "/items/new", view: () => heading("New item") };
    const anyItem = { path: "/items/:id", view: () => heading("Some item") };
    expect(matchRoute([newItem, anyItem], "/items/new")?.route, "both routes match \"/items/new\", so the first one in the list wins").toBe(newItem);
  });
});

describe("startHashRouter", () => {
  it("draws the screen for the hash the page opened with", () => {
    history.replaceState(null, "", "#/items/DC-1042");
    stop = startHashRouter(routes, outlet, notFound);
    expect(screen(), "a deep link to #/items/DC-1042 should show Golu's screen straight away").toBe("Item DC-1042");
  });

  it("draws the home page when there is no hash", () => {
    stop = startHashRouter(routes, outlet, notFound);
    expect(screen(), "with no hash, the home screen should show").toBe("Home");
  });

  it("redraws when the hash changes", () => {
    stop = startHashRouter(routes, outlet, notFound);
    goTo("#/cart");
    expect(screen(), "after the hash becomes #/cart, the cart should show").toBe("Cart");
    goTo("#/items");
    expect(screen(), "after the hash becomes #/items, the catalogue should show").toBe("Catalogue");
  });

  it("shows the not found screen for an unknown address", () => {
    stop = startHashRouter(routes, outlet, notFound);
    goTo("#/hats");
    expect(screen(), "an address no route knows should show notFound(path)").toBe("Not found: /hats");
  });

  it("replaces the old screen instead of adding to it", () => {
    stop = startHashRouter(routes, outlet, notFound);
    goTo("#/cart");
    goTo("#/items");
    expect(outlet.children.length, "the outlet should hold exactly one screen; use replaceChildren").toBe(1);
  });

  it("stops listening after stop() is called", () => {
    stop = startHashRouter(routes, outlet, notFound);
    stop();
    goTo("#/cart");
    expect(screen(), "after stop(), a hash change must not redraw the outlet").toBe("Home");
  });
});
