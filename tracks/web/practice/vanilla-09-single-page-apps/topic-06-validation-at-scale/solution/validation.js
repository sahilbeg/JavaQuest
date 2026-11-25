// Form checks for the checkout, built from small rules that any form can reuse.
// A rule is a function: it takes the typed value and returns an error message, or null when the value is fine.

/** A rule that fails when the value is empty or only spaces. */
export function required(message) {
  return (value) => (value.trim() === "" ? message : null);
}

/** A rule that fails when the trimmed value doesn't match the pattern. */
export function matches(pattern, message) {
  return (value) => (pattern.test(value.trim()) ? null : message);
}

/** The checkout form's rules: each field's rules run in order, and the first message wins. */
export const checkoutRules = {
  name: [required("Please tell us your name.")],
  phone: [required("Please give a phone number."), matches(/^\d{5} ?\d{5}$/, "A phone number has 10 digits, like 98765 43210.")],
  pin: [required("Please give your PIN code."), matches(/^[1-9]\d{5}$/, "A PIN code has 6 digits, like 411004.")],
  address: [required("Please give your address.")],
};

/** Checks every field in rules. Returns { field: message } for each field that fails, and {} when all pass. */
export function validate(values, rules) {
  const errors = {};
  for (const [field, fieldRules] of Object.entries(rules)) {
    for (const rule of fieldRules) {
      const message = rule(values[field] ?? "");
      if (message !== null) {
        errors[field] = message;
        break;
      }
    }
  }
  return errors;
}

/** Marks every named field in the form: aria-invalid, and its message in #<name>-error ("" when fine). */
export function showErrors(form, errors) {
  for (const field of form.querySelectorAll("[name]")) {
    const message = errors[field.name] ?? "";
    field.setAttribute("aria-invalid", message === "" ? "false" : "true");
    form.querySelector(`#${field.name}-error`).textContent = message;
  }
}

/** Moves focus to the first field, in form order, that has an error. Returns its name, or null when there is none. */
export function focusFirstError(form, errors) {
  const first = [...form.querySelectorAll("[name]")].find((field) => errors[field.name] !== undefined);
  if (first === undefined) {
    return null;
  }
  first.focus();
  return first.name;
}
