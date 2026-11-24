// Checks the order form before an order goes through.

const FIELDS = ["name", "phone", "pin"];

/** True when text is made only of the digits 0 to 9, and is not empty. */
export function onlyDigits(text) {
  return text !== "" && [...text].every((ch) => ch >= "0" && ch <= "9");
}

/** Checks { name, phone, pin }. Returns one message per bad field, or {} when all are fine. */
export function validate(details) {
  const errors = {};
  if (details.name.trim() === "") {
    errors.name = "Please enter your name.";
  }
  const phone = details.phone.replaceAll(" ", "");
  if (phone.length !== 10 || !onlyDigits(phone)) {
    errors.phone = "Enter a 10-digit phone number.";
  }
  const pin = details.pin.trim();
  if (pin.length !== 6 || !onlyDigits(pin)) {
    errors.pin = "Enter a 6-digit PIN code.";
  }
  return errors;
}

/** Shows "5/12" in the counter as the customer types, with the class over above max. */
export function setupStitchCounter(input, counter, max = 12) {
  function show() {
    counter.textContent = `${input.value.length}/${max}`;
    counter.classList.toggle("over", input.value.length > max);
  }
  input.addEventListener("input", show);
  show();
}

/** On submit: no reload, check the fields, show the messages, and call onOrder with clean details when all are fine. */
export function setupOrderForm(form, onOrder) {
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const data = new FormData(form);
    const details = { name: data.get("name"), phone: data.get("phone"), pin: data.get("pin") };
    const errors = validate(details);

    for (const field of FIELDS) {
      form.querySelector(`[data-error-for="${field}"]`).textContent = errors[field] ?? "";
      form.querySelector(`[name="${field}"]`).setAttribute("aria-invalid", String(field in errors));
    }

    if (Object.keys(errors).length === 0) {
      onOrder({
        name: details.name.trim(),
        phone: details.phone.replaceAll(" ", ""),
        pin: details.pin.trim(),
      });
    }
  });
}
