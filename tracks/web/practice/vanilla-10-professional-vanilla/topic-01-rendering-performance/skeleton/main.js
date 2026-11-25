// Starts the catalogue page. The checks never load this file.
import { PRODUCTS } from "./catalogue.js";
import { renderList, equalHeights, createRenderScheduler } from "./render.js";

const list = document.querySelector("#products");
const search = document.querySelector("#search");

function draw(state) {
  const words = state.search.trim().toLowerCase();
  renderList(list, PRODUCTS.filter((product) => product.name.toLowerCase().includes(words)));
  equalHeights([...list.querySelectorAll(".product")]);
}

const schedule = createRenderScheduler(draw);
search.addEventListener("input", () => schedule({ search: search.value }));
draw({ search: "" });
