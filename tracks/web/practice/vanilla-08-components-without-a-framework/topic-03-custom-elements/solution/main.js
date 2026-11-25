// Loads the badge and wires the slider. The checks never load this file.
import "./stock-badge.js";

const golu = document.querySelector("#golu");
document.querySelector("#golu-count").addEventListener("input", (event) => {
  golu.count = Number(event.target.value);
});
