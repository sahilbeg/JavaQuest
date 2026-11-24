import { useReducer, useState } from "react";
import { checkoutReducer, startCheckout } from "./checkoutReducer.js";
import { checkoutTotals } from "./totals.js";
import { validateAddress } from "./address.js";
import { rupees } from "./money.js";

export const UPI_ID = "dhaaga@upi";

/** Checkout in four steps: cart, address, UPI payment, done. placeOrder({ lines, address, total }) returns an order id. */
export function Checkout({ lines, placeOrder }) {
  // TODO 8: keep the step with useReducer(checkoutReducer, startCheckout), and show AddressStep, UpiStep or DoneStep
  //         for the "address", "upi" and "done" steps. On I've paid, call placeOrder, then dispatch "paid" with its id.
  const state = startCheckout;
  const dispatch = () => {};
  const totals = checkoutTotals(lines);

  return <CartStep lines={lines} totals={totals} onContinue={() => dispatch({ type: "cartConfirmed" })} />;
}

function CartStep({ lines, totals, onContinue }) {
  // TODO 9: list each line as "Coaster set × 2: ₹300.00", then "Items: ...", "Delivery: ..." ("Free" when it is 0) and "To pay: ...".
  //         With no lines, show "Your cart is empty." and disable the button.
  return (
    <section>
      <h2>Your cart</h2>
      <button onClick={onContinue}>Continue to address</button>
    </section>
  );
}

function AddressStep({ initial, onSave, onBack }) {
  const [values, setValues] = useState(initial ?? { name: "", phone: "", pincode: "" });

  // TODO 10: after the first submit, show validateAddress's messages. Only a valid address calls onSave,
  //          with the name and PIN code trimmed and the phone's spaces removed.
  const shown = {};

  function handleChange(e) {
    setValues({ ...values, [e.target.name]: e.target.value });
  }

  function handleSubmit(e) {
    e.preventDefault();
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
  // TODO 11: show "Pay ₹810.00 to dhaaga@upi" (the real total, and UPI_ID), "Delivering to Asha Rao, 411007",
  //          a Back button and an "I've paid" button.
  return (
    <section>
      <h2>Pay by UPI</h2>
    </section>
  );
}

function DoneStep({ orderId }) {
  // TODO 12: say "Your order id is order-17." with the real id.
  return (
    <section>
      <h2>Order placed</h2>
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
