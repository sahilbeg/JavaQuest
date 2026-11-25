import { rupees } from "./money.ts";
import type { Product, Stock } from "./products.ts";

export interface ProductCardProps {
  product: Product;
  inCart: number;
  onAdd: (id: string) => void;
}

export function stockText(stock: Stock): string {
  switch (stock.kind) {
    case "in-stock":
      return stock.count <= 3 ? `Only ${stock.count} left` : "In stock";
    case "sold-out":
      return "Sold out";
  }
}

export function ProductCard({ product, inCart, onAdd }: ProductCardProps) {
  const canAdd = product.stock.kind === "in-stock" && inCart < product.stock.count;
  return (
    <article className="product-card">
      <h3>{product.name}</h3>
      <p className="price">{rupees(product.paise)}</p>
      <p className="colour">{product.colour}</p>
      <p className="stock">{stockText(product.stock)}</p>
      <button type="button" aria-label={`Add ${product.name} to cart`} disabled={!canAdd} onClick={() => onAdd(product.id)}>
        Add
      </button>
    </article>
  );
}
