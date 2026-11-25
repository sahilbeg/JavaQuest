/** What the address form holds while the customer types. */
export interface AddressForm {
  name: string;
  phone: string;
  pincode: string;
}

/** At most one message per field. */
export type AddressErrors = Partial<Record<keyof AddressForm, string>>;

/** A checked, cleaned address. Only checkout's upi and done steps carry one. */
export interface Address {
  name: string;
  phone: string;
  pincode: string;
}

/** One message per bad field, or {} when the address is fine. */
export function validateAddress(form: AddressForm): AddressErrors {
  const errors: AddressErrors = {};
  // TODO 2: name empty after trim -> "Please enter your name."
  //         phone without spaces isn't exactly 10 digits -> "Phone needs 10 digits, like 98765 43210."
  //         pincode after trim isn't exactly 6 digits -> "PIN code needs 6 digits."
  return errors;
}

/** The cleaned address, for a form that passed validation: names trimmed, the phone without spaces. */
export function cleanAddress(form: AddressForm): Address {
  return { name: form.name.trim(), phone: form.phone.replace(/ /g, ""), pincode: form.pincode.trim() };
}
