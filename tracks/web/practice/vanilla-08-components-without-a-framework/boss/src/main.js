// The sale page: draws the grid and refreshes the stock every few seconds. The checks never load this file.
import { createGrid } from "./grid.js";

let products = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 9 },
  { id: "DC-1001", name: "Marigold coaster set", paise: 15000, stock: 14 },
  { id: "DC-1013", name: "Rani pink keychain", paise: 12000, stock: 6 },
  { id: "DC-1020", name: "Mint baby booties", paise: 35000, stock: 3 },
];

const added = [];
const cart = document.querySelector("#cart");
const grid = createGrid(document.querySelector("#grid"), ({ id, stitch }) => {
  added.push(stitch === "" ? id : `${id} (${stitch})`);
  cart.textContent = `In your cart: ${added.join(", ")}`;
});

grid.update(products);

// Every refresh, one product sells a piece, taking turns.
let turn = 0;
setInterval(() => {
  const index = turn % products.length;
  products = products.map((product, i) => (i === index ? { ...product, stock: Math.max(0, product.stock - 1) } : product));
  turn += 1;
  grid.update(products);
}, 4000);
