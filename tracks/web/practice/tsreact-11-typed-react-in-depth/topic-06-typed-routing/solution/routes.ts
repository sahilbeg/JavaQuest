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
  if (path === "/") return { page: "shop" };
  const [empty, section, id, ...rest] = path.split("/");
  if (empty === "" && id && rest.length === 0) {
    if (section === "item") return { page: "item", id };
    if (section === "order") return { page: "order", orderId: id };
  }
  return { page: "notFound", path };
}

/** The hash for a route, for links: { page: "item", id: "DC-1042" } gives "#/item/DC-1042". */
export function toHash(route: Route): string {
  switch (route.page) {
    case "shop":
      return "#/";
    case "item":
      return `#/item/${route.id}`;
    case "order":
      return `#/order/${route.orderId}`;
    case "notFound":
      return `#${route.path}`;
    default:
      return assertNever(route);
  }
}
