// One shop's stock register. The counts are private: the only way to change them is through the methods.

/** What every stock register can do, whether it's the stall's, the shop's or a test's. */
export interface StockStore {
  stockOf(id: string): number;
  receive(id: string, qty: number): void;
  sell(id: string, qty: number): boolean;
}

export class StockBook implements StockStore {
  readonly shop: string;
  private counts = new Map<string, number>();

  constructor(shop: string) {
    this.shop = shop;
  }

  /** Units of this item in stock: 0 for an item that was never received. */
  stockOf(id: string): number {
    return this.counts.get(id) ?? 0;
  }

  /**
   * Adds qty units of the item.
   * qty must be a whole number above 0; otherwise throw an Error "Quantity must be a whole number above 0".
   */
  receive(id: string, qty: number): void {
    if (!Number.isInteger(qty) || qty <= 0) {
      throw new Error("Quantity must be a whole number above 0");
    }
    this.counts.set(id, this.stockOf(id) + qty);
  }

  /** Takes qty units away and returns true. When there aren't enough, changes nothing and returns false. */
  sell(id: string, qty: number): boolean {
    const now = this.stockOf(id);
    if (now < qty) {
      return false;
    }
    this.counts.set(id, now - qty);
    return true;
  }

  /** Every unit in this shop, all items added together. */
  get totalUnits(): number {
    let total = 0;
    for (const count of this.counts.values()) {
      total += count;
    }
    return total;
  }
}
