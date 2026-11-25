import { rupees } from "./money.ts";
import type { Product, Stock } from "./products.ts";

export interface ProductCardProps {
  product: Product;
  inCart: number;
  onAdd: (id: string) => void;
}

// TODO 1: Switch on stock.kind: "In stock" for more than 3, "Only <count> left" for 3 or fewer, "Sold out" when sold out.
export function stockText(stock: Stock): string {
  return "";
}

export function ProductCard({ product, inCart, onAdd }: ProductCardProps) {
  // TODO 2: Add p.price (rupees), p.colour, p.stock (stockText), and an Add button
  //         with aria-label={`Add ${product.name} to cart`} that calls onAdd(product.id).
  // TODO 3: Disable Add when the product is sold out, or when inCart has reached its stock count.
  return (
    <article className="product-card">
      <h3>{product.name}</h3>
    </article>
  );
}
