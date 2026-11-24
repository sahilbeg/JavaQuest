// Wires the inspector to the page. The checks never load this file.
import { countProducts, productNames, firstAndLast, parentId } from "./inspect.js";

const list = document.querySelector("#products");
const summary = document.querySelector("#summary");
const ends = firstAndLast(list);

const where = `${countProducts(list)} items in #${parentId(list)}.`;
const order = ends === null ? "The list is empty." : `First: ${ends.first}. Last: ${ends.last}.`;
summary.textContent = `${where} ${order}`;

console.log(productNames(list));
