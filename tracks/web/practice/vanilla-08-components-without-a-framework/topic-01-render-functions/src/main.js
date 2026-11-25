// Draws the catalogue with your components. The checks never load this file.
import { productList } from "./cards.js";

const products = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 4 },
  { id: "DC-1001", name: "Marigold coaster set", paise: 15000, stock: 12 },
  { id: "DC-1013", name: "Rani pink keychain", paise: 12000, stock: 9 },
  { id: "DC-1007", name: "Indigo tote bag", paise: 89900, stock: 0 },
];

const status = document.querySelector("#status");

document.querySelector("#catalogue").replaceChildren(
  productList(products, (id) => {
    status.textContent = `Added ${id} to your cart`;
  }),
);
