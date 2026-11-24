import { useReducer, useState } from "react";
import { checkoutReducer, startCheckout } from "./checkoutReducer.js";
import { checkoutTotals } from "./totals.js";
import { validateAddress } from "./address.js";
import { rupees } from "./money.js";

export const UPI_ID = "dhaaga@upi";

/** Checkout in four steps: cart, address, UPI payment, done. placeOrder({ lines, address, total }) returns an order id. */
export function Checkout({ lines, placeOrder }) {
  const [state, dispatch] = useReducer(checkoutReducer, startCheckout);
  const totals = checkoutTotals(lines);

  function handlePaid() {
    const orderId = placeOrder({ lines, address: state.address, total: totals.total });
    dispatch({ type: "paid", orderId });
  }

  if (state.step === "address") {
    return (
      <AddressStep
        initial={state.address}
        onSave={(address) => dispatch({ type: "addressSaved", address })}
        onBack={() => dispatch({ type: "wentBack" })}
      />
    );
  }
  if (state.step === "upi") {
    return (
      <UpiStep total={totals.total} address={state.address} onPaid={handlePaid} onBack={() => dispatch({ type: "wentBack" })} />
    );
  }
  if (state.step === "done") {
    return <DoneStep orderId={state.orderId} />;
  }
  return <CartStep lines={lines} totals={totals} onContinue={() => dispatch({ type: "cartConfirmed" })} />;
}

function CartStep({ lines, totals, onContinue }) {
  return (
    <section>
      <h2>Your cart</h2>
      {lines.length === 0 && <p>Your cart is empty.</p>}
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
      <button onClick={onContinue} disabled={lines.length === 0}>
        Continue to address
      </button>
    </section>
  );
}

function AddressStep({ initial, onSave, onBack }) {
  const [values, setValues] = useState(initial ?? { name: "", phone: "", pincode: "" });
  const [tried, setTried] = useState(false);

  const errors = validateAddress(values);
  const shown = tried ? errors : {};

  function handleChange(e) {
    setValues({ ...values, [e.target.name]: e.target.value });
  }

  function handleSubmit(e) {
    e.preventDefault();
    setTried(true);
    if (Object.keys(errors).length > 0) return;
    onSave({ name: values.name.trim(), phone: values.phone.replaceAll(" ", ""), pincode: values.pincode.trim() });
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <h2>Delivery address</h2>
      <Field label="Name" name="name" value={values.name} error={shown.name} onChange={handleChange} />
      <Field label="Phone" name="phone" value={values.phone} error={shown.phone} onChange={handleChange} />
      <Field label="PIN code" name="pincode" value={values.pincode} error={shown.pincode} onChange={handleChange} />
      <button type="button" onClick={onBack}>
        Back
      </button>
      <button type="submit">Continue to payment</button>
    </form>
  );
}

function UpiStep({ total, address, onPaid, onBack }) {
  return (
    <section>
      <h2>Pay by UPI</h2>
      <p>
        Pay {rupees(total)} to {UPI_ID}
      </p>
      <p>
        Delivering to {address.name}, {address.pincode}
      </p>
      <button onClick={onBack}>Back</button>
      <button onClick={onPaid}>I've paid</button>
    </section>
  );
}

function DoneStep({ orderId }) {
  return (
    <section>
      <h2>Order placed</h2>
      <p>Your order id is {orderId}.</p>
    </section>
  );
}

function Field({ label, name, value, error, onChange }) {
  const errorId = `${name}-error`;
  return (
    <p>
      <label htmlFor={name}>{label}</label>
      <input
        id={name}
        name={name}
        value={value}
        onChange={onChange}
        aria-invalid={error ? "true" : undefined}
        aria-describedby={error ? errorId : undefined}
      />
      {error && <span id={errorId}>{error}</span>}
    </p>
  );
}
