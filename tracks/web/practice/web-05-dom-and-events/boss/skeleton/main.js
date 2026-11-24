// Starts the cart on the sale page. The checks never load this file.
import { createCart } from "./cart.js";
import { CATALOGUE } from "./catalogue.js";

createCart(document.querySelector("#app"), CATALOGUE);
