// The basket, with methods that use this, and a safe way to copy an order.

export function createBasket(owner) {
  return {
    owner: owner,
    lines: [],
    add(code, paise, qty = 1) {
      // TODO 1: push { code, paise, qty } onto this.lines
      return this;
    },
    total() {
      // TODO 2: add up paise * qty for every line in this.lines, starting from 0
      return 0;
    },
  };
}

export function copyOrder(order) {
  // TODO 3: this hands back the same object. Return a deep copy instead: a new order, a new customer and new lines.
  return order;
}
