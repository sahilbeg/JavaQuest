// Starts the store. The checks never load this file.
import { startStore } from "./app.js";
import { PRODUCTS } from "./catalogue.js";

startStore(document.querySelector("#app"), PRODUCTS);
