import { formatRupees } from "./money.js";

/** The stock line on a card: "Sold out", "Only N left" (1 to 3) or "In stock". */
export function stockText(stock) {
  if (stock === 0) return "Sold out";
  if (stock <= 3) return `Only ${stock} left`;
  return "In stock";
}

/**
 * Draws one product card into container, replacing what was there, and returns the <article>.
 * product looks like { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 2 }.
 */
export function renderProduct(container, product, onAdd) {
  const article = document.createElement("article");
  article.className = "product";
  article.dataset.id = product.id;

  const name = document.createElement("h3");
  name.textContent = product.name;

  const price = document.createElement("p");
  price.className = "price";
  price.textContent = formatRupees(product.paise);

  const stock = document.createElement("p");
  stock.className = "stock";
  stock.textContent = stockText(product.stock);

  const button = document.createElement("button");
  button.type = "button";
  button.textContent = "Add to cart";
  button.disabled = product.stock === 0;
  button.addEventListener("click", () => {
    if (!button.disabled) onAdd(product.id);
  });

  article.append(name, price, stock, button);
  container.replaceChildren(article);
  return article;
}
