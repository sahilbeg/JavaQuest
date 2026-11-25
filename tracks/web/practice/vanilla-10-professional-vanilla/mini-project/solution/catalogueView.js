import { el } from "./dom.js";
import { rupees } from "./money.js";
import { CATEGORIES } from "./catalogue.js";
import { filterProducts } from "./filters.js";

/** Draws the search box, the category filter, the count and the product list into section. onAdd(product) adds to the cart. */
export function mountCatalogue(section, products, onAdd) {
  const search = el("input", { id: "search", type: "search", autocomplete: "off" });
  const category = el("select", { id: "category" }, ...CATEGORIES.map((name) => el("option", { value: name }, name)));
  const count = el("p", { id: "product-count", role: "status" });
  const list = el("ul", { id: "product-list", class: "grid" });

  section.replaceChildren(
    el("h2", { id: "shop-heading" }, "Shop"),
    el("div", { class: "filters" }, el("label", { for: "search" }, "Search"), search, el("label", { for: "category" }, "Category"), category),
    count,
    list,
  );

  function productCard(product) {
    return el(
      "li",
      { class: "product", "data-id": product.id },
      el("h3", {}, product.name),
      el("p", { class: "price" }, rupees(product.paise)),
      el("button", { type: "button", class: "add", "aria-label": `Add ${product.name} to cart` }, "Add to cart"),
    );
  }

  function draw() {
    const shown = filterProducts(products, { search: search.value, category: category.value });
    list.replaceChildren(...shown.map(productCard));
    count.textContent = shown.length === 1 ? "1 product" : `${shown.length} products`;
  }

  search.addEventListener("input", draw);
  category.addEventListener("change", draw);
  list.addEventListener("click", (event) => {
    const button = event.target.closest("button.add");
    if (button === null) {
      return;
    }
    const id = button.closest(".product").dataset.id;
    onAdd(products.find((product) => product.id === id));
  });

  draw();
}
