/**
 * el("button", { type: "button", "aria-label": "Remove Golu" }, "Remove") makes an element.
 * Attributes set to true become empty attributes (hidden, novalidate); false, null and undefined are left out.
 * Text children are always text, never HTML, so customer words are safe here.
 */
export function el(tag, attributes = {}, ...children) {
  const element = document.createElement(tag);
  for (const [name, value] of Object.entries(attributes)) {
    if (value === false || value === null || value === undefined) {
      continue;
    }
    element.setAttribute(name, value === true ? "" : String(value));
  }
  element.append(...children);
  return element;
}
