// Wires Golu's item page. The checks never load this file.
import { createItemPage } from "./item-page.js";

createItemPage(document.querySelector("#item"), { id: "DC-1042", name: "Golu the elephant", paise: 45000 }, 5);
