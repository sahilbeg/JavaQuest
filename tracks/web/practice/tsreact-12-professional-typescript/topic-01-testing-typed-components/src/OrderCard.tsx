import { formatRupees } from "./money.ts";
import type { Order, OrderStatus } from "./types.ts";

const LABELS: Record<OrderStatus, string> = {
  placed: "Order placed",
  packed: "Packed, waiting for the courier",
  shipped: "On its way",
  delivered: "Delivered",
  cancelled: "Cancelled",
};

export interface OrderCardProps {
  order: Order;
  onCancel: (orderId: string) => void;
  onTrack: (orderId: string) => void;
}

/** One order on the account page. This file already works: don't change it. */
export function OrderCard({ order, onCancel, onTrack }: OrderCardProps) {
  const total = order.lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  const canCancel = order.status === "placed" || order.status === "packed";

  return (
    <article aria-labelledby={`title-${order.id}`}>
      <h3 id={`title-${order.id}`}>{order.id}</h3>
      <p>For {order.customer}</p>
      <p role="status">{LABELS[order.status]}</p>
      <p>Total: {formatRupees(total)}</p>
      {canCancel && (
        <button type="button" onClick={() => onCancel(order.id)}>
          Cancel order
        </button>
      )}
      {order.status === "shipped" && (
        <button type="button" onClick={() => onTrack(order.id)}>
          Track parcel
        </button>
      )}
    </article>
  );
}
