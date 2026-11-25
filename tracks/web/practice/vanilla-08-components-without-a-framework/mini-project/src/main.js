// Puts the pieces on the page. The checks never load this file.
import { createStore } from "./store.js";
import { createCatalogue } from "./catalogue.js";
import "./cart-count.js";

const store = createStore({
  products: [
    { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 4 },
    { id: "DC-1001", name: "Marigold coaster set", paise: 15000, stock: 12 },
    { id: "DC-1013", name: "Rani pink keychain", paise: 12000, stock: 7 },
    { id: "DC-1007", name: "Indigo tote bag", paise: 89900, stock: 1 },
  ],
  lines: [],
});

const count = document.createElement("cart-count");
count.store = store;
document.querySelector("#count-spot").append(count);

createCatalogue(document.querySelector("#catalogue"), store);

// A stock refresh: every product sells one piece, down to 0.
document.querySelector("#refresh").addEventListener("click", () => {
  store.setState((state) => ({
    products: state.products.map((product) => ({ ...product, stock: Math.max(0, product.stock - 1) })),
  }));
});
