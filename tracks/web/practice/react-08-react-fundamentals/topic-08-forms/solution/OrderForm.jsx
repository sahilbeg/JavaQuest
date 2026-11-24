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
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [code, setCode] = useState("CST");
  const [qty, setQty] = useState(1);
  const [giftWrap, setGiftWrap] = useState(false);

  const item = ITEMS.find((i) => i.code === code);
  const totalPaise = item.paise * qty + (giftWrap ? GIFT_WRAP_PAISE : 0);

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit({ name, phone, code, qty, giftWrap });
  }

  return (
    <form className="order-form" onSubmit={handleSubmit}>
      <label htmlFor="name">Your name</label>
      <input id="name" type="text" value={name} onChange={(event) => setName(event.target.value)} />

      <label htmlFor="phone">Phone</label>
      <input id="phone" type="tel" value={phone} onChange={(event) => setPhone(event.target.value)} />

      <label htmlFor="item">Item</label>
      <select id="item" value={code} onChange={(event) => setCode(event.target.value)}>
        {ITEMS.map((item) => (
          <option key={item.code} value={item.code}>
            {item.name}, {rupees(item.paise)}
          </option>
        ))}
      </select>

      <label htmlFor="qty">Quantity</label>
      <select id="qty" value={qty} onChange={(event) => setQty(Number(event.target.value))}>
        {[1, 2, 3, 4, 5].map((n) => (
          <option key={n} value={n}>
            {n}
          </option>
        ))}
      </select>

      <div className="check">
        <input id="gift-wrap" type="checkbox" checked={giftWrap} onChange={(event) => setGiftWrap(event.target.checked)} />
        <label htmlFor="gift-wrap">Gift wrap (₹30)</label>
      </div>

      <p className="total">Total: {rupees(totalPaise)}</p>

      <button type="submit" disabled={name.trim() === "" || phone.trim() === ""}>
        Place order
      </button>
    </form>
  );
}
