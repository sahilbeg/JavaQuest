/** The path from the address bar: "#/cart" gives "/cart", and no hash gives "/". */
export function currentPath() {
  return window.location.hash.slice(1) || "/";
}

/** Which page a path shows: { page: "home" | "cart" | "item" | "not-found" }, plus id for an item. */
export function matchRoute(path) {
  // TODO 1: "/" (or "") is { page: "home" }.
  // TODO 2: "/cart" is { page: "cart" }.
  // TODO 3: "/item/DC-1042" is { page: "item", id: "DC-1042" }. "/item/" with no id, and anything else, is { page: "not-found" }.
  return { page: "home" };
}
