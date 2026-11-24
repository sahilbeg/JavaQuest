/** The path from the address bar: "#/cart" gives "/cart", and no hash gives "/". */
export function currentPath() {
  return window.location.hash.slice(1) || "/";
}

/** Which page a path shows: { page: "home" | "cart" | "item" | "not-found" }, plus id for an item. */
export function matchRoute(path) {
  if (path === "/" || path === "") return { page: "home" };
  if (path === "/cart") return { page: "cart" };
  if (path.startsWith("/item/") && path.length > "/item/".length) {
    return { page: "item", id: path.slice("/item/".length) };
  }
  return { page: "not-found" };
}
