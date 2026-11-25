// <stock-badge count="3"></stock-badge> shows how many pieces are left.

export class StockBadge extends HTMLElement {
  static observedAttributes = ["count"];

  get count() {
    return Number(this.getAttribute("count") ?? 0);
  }

  set count(value) {
    this.setAttribute("count", String(value));
  }

  connectedCallback() {
    this.render();
  }

  attributeChangedCallback() {
    this.render();
  }

  render() {
    const count = this.count;
    this.classList.toggle("out", count === 0);
    this.classList.toggle("low", count > 0 && count <= 5);
    if (count === 0) {
      this.textContent = "Sold out";
    } else if (count <= 5) {
      this.textContent = `Only ${count} left`;
    } else {
      this.textContent = "In stock";
    }
  }
}

if (!customElements.get("stock-badge")) {
  customElements.define("stock-badge", StockBadge);
}
