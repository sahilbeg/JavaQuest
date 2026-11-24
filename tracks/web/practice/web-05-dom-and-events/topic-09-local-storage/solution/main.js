// Wires the PIN box, the hearts and the Forget me button. The checks never load this file.
import { savePin, loadPin, loadWishlist, toggleWish, forgetMe } from "./prefs.js";

const pinBox = document.querySelector("#pin");
const list = document.querySelector("#products");

function showHearts(ids) {
  for (const button of list.querySelectorAll(".heart")) {
    const saved = ids.includes(button.closest(".product").dataset.id);
    button.setAttribute("aria-pressed", String(saved));
    button.textContent = saved ? "♥ Saved" : "♡ Save for later";
  }
}

pinBox.value = loadPin();
pinBox.addEventListener("change", () => savePin(pinBox.value.trim()));

list.addEventListener("click", (event) => {
  const heart = event.target.closest(".heart");
  if (heart === null) {
    return;
  }
  showHearts(toggleWish(heart.closest(".product").dataset.id));
});

document.querySelector("#forget").addEventListener("click", () => {
  forgetMe();
  pinBox.value = "";
  showHearts([]);
});

showHearts(loadWishlist());
