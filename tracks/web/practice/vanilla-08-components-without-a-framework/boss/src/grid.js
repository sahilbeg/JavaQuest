// The sale page's product grid. A product looks like { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 4 }.
import { rupees } from "./money.js";

/** The stock line on a card. */
export function stockText(stock) {
  if (stock === 0) {
    return "Sold out";
  }
  if (stock <= 5) {
    return `Only ${stock} left`;
  }
  return "In stock";
}

/** One card for one product. */
function card(product) {
  const li = document.createElement("li");
  li.className = "card";
  li.dataset.id = product.id;
  li.innerHTML = `
    <h3 class="card__name"></h3>
    <p class="card__price"></p>
    <p class="card__stock"></p>
    <label class="card__stitch">Name to stitch on (₹50)
      <input class="card__stitch-input" name="stitch" maxlength="12">
    </label>
    <button class="card__add" type="button">Add to cart</button>`;
  li.querySelector(".card__name").textContent = product.name;
  li.querySelector(".card__price").textContent = rupees(product.paise);
  li.querySelector(".card__stock").textContent = stockText(product.stock);
  li.querySelector(".card__add").disabled = product.stock === 0;
  return li;
}

/**
 * Draws the product grid in root (a <ul>) and returns { update(products) }, which the page calls on every stock
 * refresh. Pressing Add to cart calls onAdd({ id, stitch }) with the product's id and the trimmed name on its card.
 */
export function createGrid(root, onAdd) {
  function update(products) {
    root.replaceChildren(...products.map(card));

    root.addEventListener("click", (event) => {
      const button = event.target.closest(".card__add");
      if (button === null) {
        return;
      }
      const li = button.closest(".card");
      onAdd({ id: li.dataset.id, stitch: li.querySelector(".card__stitch-input").value.trim() });
    });
  }

  return { update };
}
