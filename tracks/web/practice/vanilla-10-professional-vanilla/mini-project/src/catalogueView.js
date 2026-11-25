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
    // TODO 3: give the button its own name, aria-label "Add <product name> to cart",
    //         so a screen reader doesn't hear six buttons all called "Add to cart"
    return el(
      "li",
      { class: "product", "data-id": product.id },
      el("h3", {}, product.name),
      el("p", { class: "price" }, rupees(product.paise)),
      el("button", { type: "button", class: "add" }, "Add to cart"),
    );
  }

  function draw() {
    const shown = filterProducts(products, { search: search.value, category: category.value });
    list.replaceChildren(...shown.map(productCard));
    // TODO 3: say how many products show in the count line: "6 products", "1 product", "0 products"
  }

  // TODO 4: redraw when the search box changes ("input") and when the category changes ("change").
  //         Add one click listener on the list: a click on a button.add calls onAdd with that card's product
  //         (find it in products by the card's data-id).

  draw();
}
