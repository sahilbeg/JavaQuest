export type Stock =
  | { kind: "in-stock"; count: number }
  | { kind: "sold-out"; backOn: string | null }
  | { kind: "made-to-order"; days: number };

export interface StockBadgeProps {
  stock: Stock;
}

export function StockBadge({ stock }: StockBadgeProps) {
  switch (stock.kind) {
    case "in-stock":
      return stock.count <= 3 ? (
        <p className="badge badge--low">Only {stock.count} left</p>
      ) : (
        <p className="badge badge--ok">In stock</p>
      );
    case "sold-out":
      return (
        <p className="badge badge--out">
          Sold out{stock.backOn !== null && `, back on ${stock.backOn}`}
        </p>
      );
    case "made-to-order":
      return <p className="badge badge--made">Made to order: ready in {stock.days} days</p>;
    default: {
      const unhandled: never = stock;
      return unhandled;
    }
  }
}
