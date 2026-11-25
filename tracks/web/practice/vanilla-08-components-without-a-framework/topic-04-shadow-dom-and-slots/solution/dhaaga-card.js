// <dhaaga-card>: a product card whose look the page can't touch, filled by the page through slots.

const CARD_HTML = `
  <style>
    :host { display: block; max-width: 20rem; font-family: system-ui, sans-serif; }
    article { position: relative; background: #FAF7F0; border: 2px solid #2E3A8C; border-radius: 12px; padding: 1rem; color: #2E3A8C; }
    header { font-size: 1.2rem; }
    footer { font-weight: 700; margin-top: 0.5rem; }
    .badge { position: absolute; top: 0.5rem; right: 0.5rem; background: #F2A81D; border-radius: 999px; padding: 0.1rem 0.6rem; }
    ::slotted(h3) { margin: 0; color: #2E3A8C; font-family: system-ui, sans-serif; }
  </style>
  <article part="card">
    <span class="badge" part="badge" hidden>Diwali offer</span>
    <header><slot name="title"></slot></header>
    <slot></slot>
    <footer><slot name="price">Price on request</slot></footer>
  </article>
`;

export class DhaagaCard extends HTMLElement {
  static observedAttributes = ["sale"];

  constructor() {
    super();
    this.attachShadow({ mode: "open" }).innerHTML = CARD_HTML;
  }

  attributeChangedCallback() {
    this.shadowRoot.querySelector(".badge").hidden = !this.hasAttribute("sale");
  }
}

if (!customElements.get("dhaaga-card")) {
  customElements.define("dhaaga-card", DhaagaCard);
}
