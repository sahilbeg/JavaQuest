// Draws two cards and loads the badge. The checks never load this file.
import { productCard } from "./card.js";
import "./sale-badge.js";

document.querySelector("#catalogue").replaceChildren(
  productCard({ id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 4 }),
  productCard({ id: "DC-1007", name: "Indigo tote bag", paise: 89900, stock: 0 }),
);
