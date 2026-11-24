// Wires the price checker to the page. The checks never load this file.
import { productCards, productTitles, priceOf, soldOutTitles } from "./find.js";

const answer = document.querySelector("#answer");
const soldOut = soldOutTitles(document);

answer.textContent =
  `${productCards(document).length} products: ${productTitles(document).join(", ")}. ` +
  `Rani pink keychain costs ${priceOf(document, "DC-1013") || "?"}. ` +
  `Sold out: ${soldOut.length === 0 ? "nothing" : soldOut.join(", ")}.`;
