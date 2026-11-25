import { el } from "./dom.js";

/** The footer on every page: where to find Dhaaga, and the small print. */
export function siteFooter() {
  return el(
    "footer",
    { id: "contact" },
    el("h2", {}, "Visit or message us"),
    el(
      "ul",
      {},
      el("li", {}, "Aundh shop: Tuesday to Sunday, 11:00 to 21:00"),
      el("li", {}, "FC Road stall: Saturday and Sunday, 10:00 to 20:00"),
      el("li", {}, "WhatsApp: 98765 43210"),
    ),
    el(
      "nav",
      { "aria-label": "Footer" },
      el("ul", {}, el("li", {}, el("a", { href: "/delivery" }, "Delivery and returns")), el("li", {}, el("a", { href: "/privacy" }, "Privacy"))),
    ),
    el("p", {}, "© Dhaaga & Co., Pune"),
  );
}
