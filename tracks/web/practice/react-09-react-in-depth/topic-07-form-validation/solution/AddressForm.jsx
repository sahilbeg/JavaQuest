import { useState } from "react";
import { validateAddress } from "./validateAddress.js";

const EMPTY = { name: "", phone: "", line1: "", pincode: "" };

/** The delivery address form. Calls onSubmit(address) only with a valid, cleaned address. */
export function AddressForm({ onSubmit }) {
  const [values, setValues] = useState(EMPTY);
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
    onSubmit({
      name: values.name.trim(),
      phone: values.phone.replaceAll(" ", ""),
      line1: values.line1.trim(),
      pincode: values.pincode.trim(),
    });
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
      <input
        id={name}
        name={name}
        value={value}
        onChange={onChange}
        inputMode={inputMode}
        aria-invalid={error ? "true" : undefined}
        aria-describedby={error ? errorId : undefined}
      />
      {error && <span id={errorId}>{error}</span>}
    </p>
  );
}
