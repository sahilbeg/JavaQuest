/** The problems with a delivery address: { field: message } for each bad field. {} means it's fine. */
export function validateAddress({ name, phone, pincode }) {
  const errors = {};
  // TODO 2: name empty or only spaces -> "Please enter your name."
  //         phone not exactly 10 digits once spaces are removed -> "Phone needs 10 digits, like 98765 43210."
  //         trimmed pincode not 6 digits starting with 1 to 9 -> "PIN code needs 6 digits."
  return errors;
}
