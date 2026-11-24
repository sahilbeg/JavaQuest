// Draws the catalogue and redraws it when the colour filter changes. The checks never load this file.
import { renderCatalogue, countText } from "./catalogue.js";
import { CATALOGUE } from "./data.js";

const list = document.querySelector("#products");
const count = document.querySelector("#count");
const colour = document.querySelector("#colour");

function draw() {
  const shown = colour.value === "All" ? CATALOGUE : CATALOGUE.filter((product) => product.colour === colour.value);
  renderCatalogue(list, shown);
  count.textContent = countText(shown.length);
}

colour.addEventListener("change", draw);
draw();
