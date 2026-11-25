import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { cleanAddress, validateAddress } from "./address.ts";
import type { Address, AddressForm } from "./address.ts";
import { rupees } from "./money.ts";
import type { CartLine, Totals } from "./totals.ts";

export function CartStep({ lines, totals, onContinue }: { lines: CartLine[]; totals: Totals; onContinue: () => void }) {
  return (
    <section>
      <h2>Your cart</h2>
      <ul>
        {lines.map((line) => (
          <li key={line.code}>
            {line.name} × {line.qty}: {rupees(line.paise * line.qty)}
          </li>
        ))}
      </ul>
      <p>Items: {rupees(totals.items)}</p>
      <p>Delivery: {totals.delivery === 0 ? "Free" : rupees(totals.delivery)}</p>
      <p>To pay: {rupees(totals.total)}</p>
      {lines.length === 0 && <p>Your cart is empty.</p>}
      <button disabled={lines.length === 0} onClick={onContinue}>
        Continue to address
      </button>
    </section>
  );
}

export function AddressStep({
  initial,
  onSave,
  onBack,
}: {
  initial: Address | null;
  onSave: (address: Address) => void;
  onBack: () => void;
}) {
  const [form, setForm] = useState<AddressForm>(initial ?? { name: "", phone: "", pincode: "" });
  const [tried, setTried] = useState<boolean>(false);
  const errors = validateAddress(form);

  function update<K extends keyof AddressForm>(field: K, value: AddressForm[K]) {
    setForm({ ...form, [field]: value });
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setTried(true);
    if (Object.keys(errors).length === 0) onSave(cleanAddress(form));
  }

  function message(field: keyof AddressForm) {
    const text = tried ? errors[field] : undefined;
    return text ? <p role="alert">{text}</p> : null;
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <h2>Delivery address</h2>
      <label>
        Name <input value={form.name} onChange={(e: ChangeEvent<HTMLInputElement>) => update("name", e.currentTarget.value)} />
      </label>
      {message("name")}
      <label>
        Phone <input value={form.phone} onChange={(e: ChangeEvent<HTMLInputElement>) => update("phone", e.currentTarget.value)} />
      </label>
      {message("phone")}
      <label>
        PIN code <input value={form.pincode} onChange={(e: ChangeEvent<HTMLInputElement>) => update("pincode", e.currentTarget.value)} />
      </label>
      {message("pincode")}
      <button type="button" onClick={onBack}>
        Back
      </button>
      <button type="submit">Continue to payment</button>
    </form>
  );
}

export function UpiStep({
  total,
  address,
  onPaid,
  onBack,
}: {
  total: number;
  address: Address;
  onPaid: () => void;
  onBack: () => void;
}) {
  return (
    <section>
      <h2>Pay by UPI</h2>
      <p>Pay {rupees(total)} to dhaaga@upi</p>
      <p>
        Delivering to {address.name}, {address.pincode}
      </p>
      <button onClick={onBack}>Back</button>
      <button onClick={onPaid}>I've paid</button>
    </section>
  );
}

export function DoneStep({ orderId }: { orderId: string }) {
  return (
    <section>
      <h2>Order placed</h2>
      <p>Your order id is {orderId}.</p>
    </section>
  );
}
