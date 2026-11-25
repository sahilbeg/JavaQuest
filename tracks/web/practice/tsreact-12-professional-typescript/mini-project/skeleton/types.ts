/** The store's shared types. Every file imports its shapes from here. */

export type ItemCode = "CST" | "KEY" | "AMI" | "BAG" | "BTY";

export type Category = "Toys" | "Home" | "Accessories" | "Baby";

/** A choice in the Category filter: a real category, or "All". */
export type CategoryFilter = Category | "All";

export interface Product {
  id: string;
  code: ItemCode;
  name: string;
  category: Category;
  paise: number;
}

/** One line in the cart. Money is whole paise. */
export interface CartLine {
  id: string;
  name: string;
  paise: number;
  qty: number;
}

/** Everything that can happen to the cart. */
export type CartAction =
  | { type: "add"; product: Product }
  | { type: "setQty"; id: string; qty: number }
  | { type: "remove"; id: string }
  | { type: "clear" };

/** The money in a cart, in paise. */
export interface Totals {
  subtotal: number;
  discount: number;
  delivery: number;
  total: number;
}

export interface Address {
  name: string;
  phone: string;
  pin: string;
}

/** The checkout's steps, in order. */
export type Step = "Address" | "Payment" | "Done";

/** What Checkout reports when the customer has paid. */
export interface PlacedOrder {
  orderId: string;
  totalPaise: number;
  payment: "UPI";
}
