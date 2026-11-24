import { useState } from "react";
import { rupees } from "./money.js";

export const ITEMS = [
  { code: "CST", name: "Coaster set (4)", paise: 15000 },
  { code: "KEY", name: "Keychain", paise: 12000 },
  { code: "AMI", name: "Amigurumi (small toy)", paise: 45000 },
  { code: "BAG", name: "Tote bag", paise: 89900 },
  { code: "BTY", name: "Baby booties", paise: 35000 },
];

export const GIFT_WRAP_PAISE = 3000;

export function OrderForm({ onSubmit }) {
  // TODO 1: Keep each field in state: name (""), phone (""), code ("CST"), qty (1, a number) and giftWrap (false).

  // TODO 2: Work out the total in paise from state: the chosen item's price times qty,
  //         plus GIFT_WRAP_PAISE when gift wrap is ticked.
  const totalPaise = 0;

  function handleSubmit(event) {
    // TODO 3: Stop the page from reloading, then call onSubmit with { name, phone, code, qty, giftWrap }.
  }

  return (
    <form className="order-form" onSubmit={handleSubmit}>
      {/* TODO 4: Make every field controlled: value (checked for the checkbox) from state, and an onChange
          that stores the new value. Store qty as a number. */}
      <label htmlFor="name">Your name</label>
      <input id="name" type="text" />

      <label htmlFor="phone">Phone</label>
      <input id="phone" type="tel" />

      <label htmlFor="item">Item</label>
      <select id="item">
        {ITEMS.map((item) => (
          <option key={item.code} value={item.code}>
            {item.name}, {rupees(item.paise)}
          </option>
        ))}
      </select>

      <label htmlFor="qty">Quantity</label>
      <select id="qty">
        {[1, 2, 3, 4, 5].map((n) => (
          <option key={n} value={n}>
            {n}
          </option>
        ))}
      </select>

      <div className="check">
        <input id="gift-wrap" type="checkbox" />
        <label htmlFor="gift-wrap">Gift wrap (₹30)</label>
      </div>

      <p className="total">Total: {rupees(totalPaise)}</p>

      {/* TODO 5: Disable Place order while the name or the phone is blank (only spaces counts as blank). */}
      <button type="submit">Place order</button>
    </form>
  );
}
