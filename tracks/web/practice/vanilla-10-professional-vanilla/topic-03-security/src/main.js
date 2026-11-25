// Starts the gift-note page. The checks never load this file.
import { packingSlipHtml, showGiftNote, nextPage } from "./giftNote.js";

const form = document.querySelector("#note-form");
const card = document.querySelector("#card");
const slip = document.querySelector("#slip");

// The note from yesterday's order. Before the fix, its image tag runs code and shows the red warning.
form.elements.to.value = "Asha & Ravi";
form.elements.message.value = `Happy Diwali! <img src="x" onerror="document.querySelector('#warning').hidden = false">`;
form.elements.from.value = "Meera";

function update() {
  const note = { to: form.elements.to.value, message: form.elements.message.value, from: form.elements.from.value };
  showGiftNote(card, note);
  slip.textContent = packingSlipHtml(note);
}

form.addEventListener("input", update);
update();

document.querySelector("#continue").setAttribute("href", nextPage(location.search));
