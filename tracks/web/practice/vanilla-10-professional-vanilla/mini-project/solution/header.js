import { el } from "./dom.js";

/** Draws the header: a skip link, the shop's name, the main menu and a live cart count that follows the store. */
export function mountHeader(header, store) {
  const count = el("p", { id: "cart-count", role: "status" });
  header.replaceChildren(
    el("a", { class: "skip-link", href: "#main" }, "Skip to content"),
    el("a", { class: "brand", href: "/" }, "Dhaaga & Co."),
    el("nav", { "aria-label": "Main" }, el("ul", {}, el("li", {}, el("a", { href: "#main" }, "Shop")), el("li", {}, el("a", { href: "#contact" }, "Contact")))),
    count,
  );

  function render(lines) {
    const items = lines.reduce((sum, line) => sum + line.qty, 0);
    count.textContent = `Cart: ${items} ${items === 1 ? "item" : "items"}`;
  }

  store.subscribe(render);
  render(store.getState());
}
