// The basket, with methods that use this, and a safe way to copy an order.

export function createBasket(owner) {
  return {
    owner: owner,
    lines: [],
    add(code, paise, qty = 1) {
      this.lines.push({ code, paise, qty });
      return this;
    },
    total() {
      return this.lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
    },
  };
}

export function copyOrder(order) {
  return {
    ...order,
    customer: { ...order.customer },
    lines: order.lines.map((line) => ({ ...line })),
  };
}
