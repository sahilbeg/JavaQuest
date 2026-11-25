// <stock-badge count="3"></stock-badge> shows how many pieces are left.

export class StockBadge extends HTMLElement {
  // TODO 1: static observedAttributes = [...] with "count", so attributeChangedCallback runs when it changes

  // TODO 2: the getter returns the count attribute as a number (0 when missing); the setter sets the attribute
  get count() {
    return 0;
  }

  set count(value) {}

  connectedCallback() {
    // TODO 3: draw the badge when it joins the page
  }

  attributeChangedCallback() {
    this.render();
  }

  render() {
    // TODO 4: 0 gives "Sold out" and the class out; 1 to 5 give "Only N left" and the class low; above 5 gives "In stock"
  }
}

// TODO 5: if customElements.get("stock-badge") is undefined, define "stock-badge" as StockBadge
