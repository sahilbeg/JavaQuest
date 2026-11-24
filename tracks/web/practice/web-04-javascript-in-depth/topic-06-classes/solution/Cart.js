// One Cart class for every page of dhaaga.in.

export class Cart {
  #lines = [];

  constructor(customer) {
    this.customer = customer;
  }

  add(item, qty = 1) {
    const line = this.#lines.find((l) => l.id === item.id);
    if (line) {
      line.qty = line.qty + qty;
    } else {
      this.#lines.push({ id: item.id, name: item.name, paise: item.paise, qty });
    }
    return this;
  }

  remove(id) {
    this.#lines = this.#lines.filter((line) => line.id !== id);
  }

  lines() {
    return this.#lines.map((line) => ({ ...line }));
  }

  get count() {
    return this.#lines.reduce((sum, line) => sum + line.qty, 0);
  }

  get total() {
    return this.#lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  }
}
