import { el } from "./dom.js";

/** Draws the header: a skip link, the shop's name, the main menu and a live cart count that follows the store. */
export function mountHeader(header, store) {
  const count = el("p", { id: "cart-count", role: "status" }, "Cart");
  // TODO 12: start the header with a skip link, <a class="skip-link" href="#main">Skip to content</a>, before the brand.
  //          Make the menu a <nav aria-label="Main"> (a list of the same two links is nicest).
  //          Keep count in step with the store: subscribe, and write "Cart: 0 items", "Cart: 1 item", "Cart: 3 items",
  //          counting every line's qty. Draw it once at the start, too.
  header.replaceChildren(
    el("a", { class: "brand", href: "/" }, "Dhaaga & Co."),
    el("div", { class: "menu" }, el("a", { href: "#main" }, "Shop"), " ", el("a", { href: "#contact" }, "Contact")),
    count,
  );
}
