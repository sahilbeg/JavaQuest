import { useState } from "react";
import type { ChangeEvent, SubmitEvent } from "react";
import { GIFT_WRAP_PAISE, PRICES, isItemCode } from "./items.ts";
import type { ItemCode } from "./items.ts";
import { rupees } from "./money.ts";

export interface OrderRequest {
  name: string;
  item: ItemCode;
  qty: number;
  giftWrap: boolean;
}

export const EMPTY_REQUEST: OrderRequest = { name: "", item: "AMI", qty: 1, giftWrap: false };

export interface CustomOrderFormProps {
  onSend: (request: OrderRequest) => void;
}

export function CustomOrderForm({ onSend }: CustomOrderFormProps) {
  const [form, setForm] = useState<OrderRequest>(EMPTY_REQUEST);
  const [error, setError] = useState("");

  // TODO 1: Copy the form with one field changed: setForm({ ...form, [key]: value }).
  function update<K extends keyof OrderRequest>(key: K, value: OrderRequest[K]) {}

  // TODO 2: Check e.target.value with isItemCode, and only then update("item", ...).
  function handleItem(e: ChangeEvent<HTMLSelectElement>) {}

  // TODO 6: If the name is empty (after trim), show "Please enter your name" and stop.
  //         Otherwise clear the error and call onSend(form).
  function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
  }

  // TODO 5: The item's price times the quantity, plus GIFT_WRAP_PAISE when gift wrap is ticked.
  const total = PRICES[form.item];

  return (
    <form className="order-form" onSubmit={handleSubmit}>
      <label htmlFor="name">Your name</label>
      <input id="name" value={form.name} onChange={(e) => update("name", e.target.value)} />

      <label htmlFor="item">Item</label>
      <select id="item" value={form.item} onChange={handleItem}>
        <option value="CST">Coaster set (4)</option>
        <option value="KEY">Keychain</option>
        <option value="AMI">Amigurumi</option>
        <option value="BAG">Tote bag</option>
        <option value="BTY">Baby booties</option>
      </select>

      <label htmlFor="qty">Quantity</label>
      {/* TODO 3: Add onChange that stores the quantity as a number: update("qty", Number(e.target.value)). */}
      <select id="qty" value={form.qty}>
        <option value="1">1</option>
        <option value="2">2</option>
        <option value="3">3</option>
        <option value="4">4</option>
        <option value="5">5</option>
      </select>

      {/* TODO 4: Add onChange that stores whether the box is ticked: update("giftWrap", e.target.checked). */}
      <input id="gift" type="checkbox" checked={form.giftWrap} />
      <label htmlFor="gift">Gift wrap (₹30)</label>

      <p className="total">Total: {rupees(total)}</p>
      <p className="error">{error}</p>
      <button type="submit">Send request</button>
    </form>
  );
}
