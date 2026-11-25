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

  function update<K extends keyof OrderRequest>(key: K, value: OrderRequest[K]) {
    setForm({ ...form, [key]: value });
  }

  function handleItem(e: ChangeEvent<HTMLSelectElement>) {
    const value = e.target.value;
    if (isItemCode(value)) {
      update("item", value);
    }
  }

  function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    if (form.name.trim() === "") {
      setError("Please enter your name");
      return;
    }
    setError("");
    onSend(form);
  }

  const total = PRICES[form.item] * form.qty + (form.giftWrap ? GIFT_WRAP_PAISE : 0);

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
      <select id="qty" value={form.qty} onChange={(e) => update("qty", Number(e.target.value))}>
        <option value="1">1</option>
        <option value="2">2</option>
        <option value="3">3</option>
        <option value="4">4</option>
        <option value="5">5</option>
      </select>

      <input id="gift" type="checkbox" checked={form.giftWrap} onChange={(e) => update("giftWrap", e.target.checked)} />
      <label htmlFor="gift">Gift wrap (₹30)</label>

      <p className="total">Total: {rupees(total)}</p>
      <p className="error">{error}</p>
      <button type="submit">Send request</button>
    </form>
  );
}
