import { memo } from "react";
import { rupees } from "./money.ts";
import type { PastOrder } from "./orders.ts";

/** Every render of a row adds its order id here, so the checks (and the preview) can count renders. */
export const rowRenders: string[] = [];

export interface OrderRowProps {
  order: PastOrder;
  onOpen: (id: string) => void;
}

export const OrderRow = memo(function OrderRow({ order, onOpen }: OrderRowProps) {
  rowRenders.push(order.id);
  return (
    <li>
      {order.id}, {order.customer}, {rupees(order.totalPaise)} <button onClick={() => onOpen(order.id)}>Open {order.id}</button>
    </li>
  );
});
