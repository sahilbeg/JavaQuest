import { useState } from "react";
import { validateAddress } from "./validateAddress.js";

const EMPTY = { name: "", phone: "", line1: "", pincode: "" };

/** The delivery address form. Calls onSubmit(address) only with a valid, cleaned address. */
export function AddressForm({ onSubmit }) {
  const [values, setValues] = useState(EMPTY);
  const [tried, setTried] = useState(false);

  // TODO 5: work out the errors with validateAddress(values), and show them only once tried is true.
  const shown = {};

  function handleChange(e) {
    setValues({ ...values, [e.target.name]: e.target.value });
  }

  function handleSubmit(e) {
    // TODO 6: stop the page reloading, set tried, and call onSubmit with cleaned values only if there are no errors.
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <Field label="Name" name="name" value={values.name} error={shown.name} onChange={handleChange} />
      <Field label="Phone" name="phone" value={values.phone} error={shown.phone} onChange={handleChange} inputMode="tel" />
      <Field label="House and street" name="line1" value={values.line1} error={shown.line1} onChange={handleChange} />
      <Field label="PIN code" name="pincode" value={values.pincode} error={shown.pincode} onChange={handleChange} inputMode="numeric" />
      <button type="submit">Save address</button>
    </form>
  );
}

function Field({ label, name, value, error, onChange, inputMode }) {
  const errorId = `${name}-error`;
  return (
    <p>
      <label htmlFor={name}>{label}</label>
      {/* TODO 7: when there is an error, add aria-invalid="true" and aria-describedby={errorId} to this input. */}
      <input id={name} name={name} value={value} onChange={onChange} inputMode={inputMode} />
      {error && <span id={errorId}>{error}</span>}
    </p>
  );
}
