// Draws the catalogue from the template. The checks never load this file.
import { renderCards } from "./cards.js";

const products = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, photo: "photos/DC-1042.jpg" },
  { id: "DC-1001", name: "Marigold coaster set", paise: 15000, photo: "photos/DC-1001.jpg" },
  { id: "DC-1013", name: "Rani pink keychain", paise: 12000, photo: "photos/DC-1013.jpg" },
];

renderCards(document.querySelector("#catalogue"), document.querySelector("#product-card"), products);
