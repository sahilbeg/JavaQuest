// Starts the search box with suggestions. The checks never load this file.
import { createSuggestions } from "./suggest.js";

const NAMES = [
  "Golu the elephant",
  "Marigold coaster set",
  "Indigo coaster set",
  "Mint keychain",
  "Rani pink tote bag",
  "Off-white baby booties",
];

const picked = document.querySelector("#picked");

createSuggestions({
  input: document.querySelector("#search"),
  listbox: document.querySelector("#suggestions"),
  status: document.querySelector("#search-status"),
  names: NAMES,
  onPick: (name) => {
    picked.textContent = `Showing ${name}`;
  },
});
