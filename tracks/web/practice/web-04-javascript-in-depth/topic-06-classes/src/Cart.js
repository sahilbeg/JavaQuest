// One Cart class for every page of dhaaga.in.

export class Cart {
  #lines = [];

  constructor(customer) {
    // TODO 1: store customer on the new cart as this.customer
  }

  add(item, qty = 1) {
    // TODO 2: find a line with the same id as item. If there is one, add qty to it.
    // Otherwise push a new line { id, name, paise, qty } onto this.#lines.
    return this;
  }

  remove(id) {
    // TODO 3: keep only the lines whose id is not this id
  }

  lines() {
    // TODO 4: return a copy of every line, so code outside can't change the cart
    return [];
  }

  get count() {
    // TODO 5: add up qty over all lines
    return 0;
  }

  get total() {
    // TODO 6: add up paise * qty over all lines
    return 0;
  }
}
