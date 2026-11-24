// Fills the three cards on the page. The checks never load this file.
import { setName, setPrice, showStock } from "./card.js";

const items = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 2 },
  { id: "DC-1007", name: "Indigo tote bag", paise: 89900, stock: 0 },
  { id: "DC-1013", name: "Rani pink keychain", paise: 12000, stock: 14 },
];

for (const item of items) {
  const card = document.querySelector(`.product[data-id="${item.id}"]`);
  setName(card, item.name);
  setPrice(card, item.paise);
  showStock(card, item.stock);
}
