/** Every screen of the store, and what each one needs. */
export type Route =
  | { page: "shop" }
  | { page: "item"; id: string }
  | { page: "order"; orderId: string }
  | { page: "notFound"; path: string };

/** Only callable with never: a type error if a page is missing a case. */
export function assertNever(value: never): never {
  throw new Error(`Unknown route: ${JSON.stringify(value)}`);
}

/** "#/item/DC-1042" becomes "/item/DC-1042". An empty hash, or just "#", becomes "/". */
export function pathFromHash(hash: string): string {
  const path = hash.replace(/^#/, "");
  return path === "" ? "/" : path;
}

/** The route for a path: "/item/DC-1042" gives { page: "item", id: "DC-1042" }. */
export function parseRoute(path: string): Route {
  // TODO 1: "/" is the shop. Split the path on "/". Exactly three parts, starting with "" and with a non-empty
  //         last part, make { page: "item", id } for "item" or { page: "order", orderId } for "order".
  //         Anything else is { page: "notFound", path }.
  return { page: "notFound", path };
}

/** The hash for a route, for links: { page: "item", id: "DC-1042" } gives "#/item/DC-1042". */
export function toHash(route: Route): string {
  // TODO 2: shop is "#/", item is "#/item/<id>", order is "#/order/<orderId>", notFound is "#" + path.
  return "#/";
}
