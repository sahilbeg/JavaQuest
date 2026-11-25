import { colourBySlug } from "./colours.js";

/** The items matching the shop's choices, as a new array. colour is a slug, q a search, sort "price-asc" or "price-desc". */
export function queryCatalogue(items, { colour = "", q = "", sort = "" } = {}) {
  let result = [...items];
  if (colour) {
    const found = colourBySlug(colour);
    if (!found) return [];
    result = result.filter((item) => item.colour === found.name);
  }
  const search = q.trim().toLowerCase();
  if (search) result = result.filter((item) => item.name.toLowerCase().includes(search));
  if (sort === "price-asc") result.sort((a, b) => a.paise - b.paise);
  if (sort === "price-desc") result.sort((a, b) => b.paise - a.paise);
  return result;
}

/** The shop's address for these choices, like "/items?colour=indigo&sort=price-asc". Empty choices are left out. */
export function shopHref({ colour = "", q = "", sort = "" } = {}) {
  const params = new URLSearchParams();
  if (colour) params.set("colour", colour);
  if (q) params.set("q", q);
  if (sort) params.set("sort", sort);
  const query = params.toString();
  return query ? `/items?${query}` : "/items";
}
