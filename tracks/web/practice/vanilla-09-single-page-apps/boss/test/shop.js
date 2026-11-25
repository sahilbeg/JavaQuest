// A small shop for the checks: four routes, an outlet, and the browser's Back and Forward.
import { createRouter } from "../src/router.js";

export function heading(text) {
  const h1 = document.createElement("h1");
  h1.textContent = text;
  return h1;
}

export const routes = [
  { path: "/", title: () => "Home", view: () => heading("Home") },
  {
    path: "/items",
    title: () => "Catalogue",
    view: (params, query) => heading(query.get("colour") ? `Catalogue: ${query.get("colour")}` : "Catalogue"),
  },
  { path: "/items/:id", title: ({ id }) => `Item ${id}`, view: ({ id }) => heading(`Item ${id}`) },
  { path: "/cart", title: () => "Cart", view: () => heading("Cart") },
];

export const notFound = (path) => heading(`Not found: ${path}`);

/** A fresh page that opens at path, with a router that hasn't started yet. */
export function freshShop(path = "/") {
  history.replaceState(null, "", path);
  document.title = "";
  document.body.innerHTML = `<main id="app"></main>`;
  const outlet = document.querySelector("#app");
  const router = createRouter(routes, outlet, notFound);
  return { router, screen: () => outlet.textContent };
}

/** What the browser does on Back or Forward: the address changes first, then popstate fires. */
export function browserMovesTo(path) {
  history.replaceState(null, "", path);
  window.dispatchEvent(new PopStateEvent("popstate"));
}
