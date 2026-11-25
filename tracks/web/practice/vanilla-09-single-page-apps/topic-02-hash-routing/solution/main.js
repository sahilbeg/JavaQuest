// Starts the router for the preview. The checks never load this file.
import { routes, notFoundView } from "./views.js";
import { startHashRouter } from "./router.js";

startHashRouter(routes, document.querySelector("#app"), notFoundView);
