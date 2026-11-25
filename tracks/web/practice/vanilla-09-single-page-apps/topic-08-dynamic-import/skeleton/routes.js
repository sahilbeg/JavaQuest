// The store's routes. The catalogue is part of the first download; the checkout's code arrives only when it is opened.
import { lazy } from "./lazy.js";
import { catalogueView } from "./catalogue.js";
import checkoutView from "./checkout.js";

// TODO 5: the checkout is big, and most customers never open it. Delete the static import of checkout.js above,
//         and give the checkout route a load function made with lazy and a dynamic import of "./checkout.js",
//         instead of view: checkoutView.
export const routes = [
  { path: "/items", view: catalogueView },
  { path: "/checkout", view: checkoutView },
];
