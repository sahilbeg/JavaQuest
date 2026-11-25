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
      // TODO 1: With 3 or fewer left, show "Only {stock.count} left" with className "badge badge--low".
      return <p className="badge badge--ok">In stock</p>;
    // TODO 2: case "sold-out": "Sold out", plus ", back on <backOn>" when backOn is not null. Classes "badge badge--out".
    // TODO 3: case "made-to-order": "Made to order: ready in <days> days". Classes "badge badge--made".
    // TODO 4: default: const unhandled: never = stock; return unhandled;
  }
}
