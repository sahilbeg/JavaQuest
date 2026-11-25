import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { COLOURS, EMPTY_FORM, isColour, toRequest, validateRequest } from "./request.ts";
import type { CustomRequest, RequestForm } from "./request.ts";

/** The custom amigurumi request form. */
export function CustomOrderForm({ onSubmit }: { onSubmit: (request: CustomRequest) => void }) {
  const [form, setForm] = useState<RequestForm>(EMPTY_FORM);
  const [tried, setTried] = useState<boolean>(false);
  const errors = validateRequest(form);

  /** Sets one field. TypeScript checks that the value fits that field. */
  function update<K extends keyof RequestForm>(field: K, value: RequestForm[K]) {
    // TODO 3: set the form to a copy with this one field changed: { ...form, [field]: value }.
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    // TODO 4: setTried(true). If errors has no keys, call onSubmit(toRequest(form)).
  }

  /** The message for a field, but only after the first try. */
  function message(field: keyof RequestForm) {
    const text = tried ? errors[field] : undefined;
    return text ? <p role="alert">{text}</p> : null;
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <label>
        Name <input value={form.name} onChange={(e: ChangeEvent<HTMLInputElement>) => update("name", e.currentTarget.value)} />
      </label>
      {message("name")}
      <label>
        Phone <input value={form.phone} onChange={(e: ChangeEvent<HTMLInputElement>) => update("phone", e.currentTarget.value)} />
      </label>
      {message("phone")}
      <label>
        Colour{" "}
        <select
          value={form.colour}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => {
            const value = e.currentTarget.value;
            if (isColour(value)) update("colour", value);
          }}
        >
          {COLOURS.map((colour) => (
            <option key={colour}>{colour}</option>
          ))}
        </select>
      </label>
      <label>
        How many <input value={form.qty} onChange={(e: ChangeEvent<HTMLInputElement>) => update("qty", e.currentTarget.value)} />
      </label>
      {message("qty")}
      <label>
        Name to stitch on (optional){" "}
        <input value={form.stitchedName} onChange={(e: ChangeEvent<HTMLInputElement>) => update("stitchedName", e.currentTarget.value)} />
      </label>
      {message("stitchedName")}
      <button type="submit">Send request</button>
    </form>
  );
}
