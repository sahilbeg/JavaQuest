// <sale-badge>Diwali offer</sale-badge>: a small badge whose colours pages may change from outside.

const BADGE_HTML = `
  <style>
    :host { display: inline-block; padding: 0.2rem 0.6rem; border-radius: 999px; background: var(--sale-badge-bg, #F2A81D); color: var(--sale-badge-text, #2E3A8C); font-weight: 700; }
    :host([hidden]) { display: none; }
  </style>
  <span part="label"><slot>Diwali offer</slot></span>
`;

export class SaleBadge extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" }).innerHTML = BADGE_HTML;
  }
}

if (!customElements.get("sale-badge")) {
  customElements.define("sale-badge", SaleBadge);
}
