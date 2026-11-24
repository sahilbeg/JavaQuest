// Checks the order form before an order goes through.

const FIELDS = ["name", "phone", "pin"];

/** True when text is made only of the digits 0 to 9, and is not empty. */
export function onlyDigits(text) {
  return text !== "" && [...text].every((ch) => ch >= "0" && ch <= "9");
}

/** Checks { name, phone, pin }. Returns one message per bad field, or {} when all are fine. */
export function validate(details) {
  const errors = {};
  // TODO 1: if details.name is "" after trim, set errors.name = "Please enter your name."
  // TODO 2: remove the spaces from details.phone; unless it is 10 digits, set errors.phone = "Enter a 10-digit phone number."
  // TODO 3: trim details.pin; unless it is 6 digits, set errors.pin = "Enter a 6-digit PIN code."
  return errors;
}

/** Shows "5/12" in the counter as the customer types, with the class over above max. */
export function setupStitchCounter(input, counter, max = 12) {
  function show() {
    // TODO 4: write `${input.value.length}/${max}` into counter, and toggle the class over when the length is above max
  }
  // TODO 5: run show on every input event of the input
  show();
}

/** On submit: no reload, check the fields, show the messages, and call onOrder with clean details when all are fine. */
export function setupOrderForm(form, onOrder) {
  // TODO 6: listen for submit on the form, and call event.preventDefault() first
  // TODO 7: read name, phone and pin with new FormData(form) and validate them; for each of FIELDS, write errors[field] ?? "" into its [data-error-for] element and set aria-invalid on its [name] input
  // TODO 8: if there are no errors, call onOrder({ name, phone, pin }) with name and pin trimmed and the phone without spaces
}
