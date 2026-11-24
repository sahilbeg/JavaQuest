/** The problems with a delivery address: { field: message } for each bad field. {} means it's fine. */
export function validateAddress({ name, phone, line1, pincode }) {
  const errors = {};
  if (name.trim() === "") errors.name = "Please enter your name.";
  if (!/^\d{10}$/.test(phone.replaceAll(" ", ""))) errors.phone = "Phone needs 10 digits, like 98765 43210.";
  if (line1.trim() === "") errors.line1 = "Please enter your house and street.";
  if (!/^[1-9]\d{5}$/.test(pincode.trim())) errors.pincode = "PIN code needs 6 digits.";
  return errors;
}
