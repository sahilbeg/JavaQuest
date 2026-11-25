import { colourBySlug } from "./colours.js";

/** The items matching the shop's choices, as a new array. colour is a slug, q a search, sort "price-asc" or "price-desc". */
export function queryCatalogue(items, { colour = "", q = "", sort = "" } = {}) {
  // TODO 1: copy items, keep the colour (colourBySlug gives its name; unknown slug: []), keep names containing q
  // (trimmed, ignoring case), then sort the copy for "price-asc" or "price-desc".
  return [];
}

/** The shop's address for these choices, like "/items?colour=indigo&sort=price-asc". Empty choices are left out. */
export function shopHref({ colour = "", q = "", sort = "" } = {}) {
  // TODO 2: build the query with URLSearchParams, in the order colour, q, sort. No choices: "/items".
  return "/items";
}
