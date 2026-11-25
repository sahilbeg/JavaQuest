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
    // TODO 1: read the count from this.counts, or 0 when there is none.
    return 0;
  }

  /**
   * Adds qty units of the item.
   * qty must be a whole number above 0; otherwise throw an Error "Quantity must be a whole number above 0".
   */
  receive(id: string, qty: number): void {
    // TODO 2: check qty, then add it to the item's count.
  }

  /** Takes qty units away and returns true. When there aren't enough, changes nothing and returns false. */
  sell(id: string, qty: number): boolean {
    // TODO 3: compare with stockOf(id) before changing anything.
    return false;
  }

  /** Every unit in this shop, all items added together. */
  get totalUnits(): number {
    // TODO 4: add up every count in this.counts.
    return 0;
  }
}
