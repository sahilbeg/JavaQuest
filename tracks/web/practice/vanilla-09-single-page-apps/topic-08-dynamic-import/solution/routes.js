// The store's routes. The catalogue is part of the first download; the checkout's code arrives only when it is opened.
import { lazy } from "./lazy.js";
import { catalogueView } from "./catalogue.js";

export const routes = [
  { path: "/items", view: catalogueView },
  { path: "/checkout", load: lazy(() => import("./checkout.js")) },
];
