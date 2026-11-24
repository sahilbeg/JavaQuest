// Starts the shop page with its cart drawer. The checks never load this file.
import { createDrawer } from "./drawer.js";
import { CATALOGUE } from "./catalogue.js";

createDrawer(document.querySelector("#app"), CATALOGUE, []);
