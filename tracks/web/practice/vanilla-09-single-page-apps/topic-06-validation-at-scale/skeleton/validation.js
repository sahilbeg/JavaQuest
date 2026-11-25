// Form checks for the checkout, built from small rules that any form can reuse.
// A rule is a function: it takes the typed value and returns an error message, or null when the value is fine.

/** A rule that fails when the value is empty or only spaces. */
export function required(message) {
  // TODO 1: return a rule (value) => ... that gives message when value.trim() is "", and null otherwise.
  return (value) => null;
}

/** A rule that fails when the trimmed value doesn't match the pattern. */
export function matches(pattern, message) {
  // TODO 2: return a rule that gives null when pattern.test(value.trim()) is true, and message otherwise.
  return (value) => null;
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
  // TODO 3: for each [field, fieldRules] of Object.entries(rules), run the rules in order on values[field]
  //         (use "" when the value is missing). Save the first message that isn't null, then stop that field.
  return errors;
}

/** Marks every named field in the form: aria-invalid, and its message in #<name>-error ("" when fine). */
export function showErrors(form, errors) {
  // TODO 4: for every element matching "[name]" in the form, set aria-invalid to "true" when errors has
  //         a message for its name, or "false" when not, and put the message (or "") in #<name>-error.
}

/** Moves focus to the first field, in form order, that has an error. Returns its name, or null when there is none. */
export function focusFirstError(form, errors) {
  // TODO 5: find the first "[name]" element whose name has an error, call .focus() on it and return its name.
  return null;
}
