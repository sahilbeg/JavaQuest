/** The problems with a delivery address: { field: message } for each bad field. {} means it's fine. */
export function validateAddress({ name, phone, line1, pincode }) {
  const errors = {};
  // TODO 1: name empty or only spaces -> errors.name = "Please enter your name."
  // TODO 2: phone not exactly 10 digits after removing spaces -> errors.phone = "Phone needs 10 digits, like 98765 43210."
  // TODO 3: line1 empty or only spaces -> errors.line1 = "Please enter your house and street."
  // TODO 4: trimmed pincode not 6 digits starting with 1 to 9 -> errors.pincode = "PIN code needs 6 digits."
  return errors;
}
