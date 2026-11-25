// <dhaaga-card>: a product card whose look the page can't touch, filled by the page through slots.

// TODO 2: start CARD_HTML with a <style> holding a :host rule (display: block), and wrap the rest in <article part="card">
// TODO 3: inside the article: a <header> with <slot name="title"></slot>, a default <slot></slot>,
//         and a <footer> with <slot name="price">Price on request</slot>
const CARD_HTML = `
  <span class="badge" part="badge" hidden>Diwali offer</span>
`;

export class DhaagaCard extends HTMLElement {
  static observedAttributes = ["sale"];

  constructor() {
    super();
    // TODO 1: attach an open shadow root and set its innerHTML to CARD_HTML
  }

  attributeChangedCallback() {
    // TODO 4: show the shadow root's .badge only while the card has a sale attribute (hasAttribute)
  }
}

if (!customElements.get("dhaaga-card")) {
  customElements.define("dhaaga-card", DhaagaCard);
}
