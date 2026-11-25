// The custom-order form, with types that follow the form by themselves.

export interface OrderForm {
  name: string;
  phone: string;
  pincode: string;
  qty: number;
}

/** One optional message for every field of T. */
export type FormErrors<T> = { [K in keyof T]?: string };

/** Item ids look like DC-1042. */
export type ItemId = `DC-${number}`;

/** true when text is an item id: DC- followed by one or more digits, and nothing else. */
export function isItemId(text: string): text is ItemId {
  return /^DC-\d+$/.test(text);
}

/** A new object with the same keys as obj, where each value is fn(value, key). obj is not changed. */
export function mapValues<T extends object, R>(
  obj: T,
  fn: (value: T[keyof T], key: keyof T) => R,
): { [K in keyof T]: R } {
  const result = {} as { [K in keyof T]: R };
  for (const key of Object.keys(obj) as (keyof T)[]) {
    result[key] = fn(obj[key], key);
  }
  return result;
}

/**
 * The problems with the form, one message per bad field. {} when everything is fine.
 *   name: empty or only spaces                         -> "Please enter your name"
 *   phone: not exactly 10 digits once spaces are removed -> "Phone needs 10 digits"
 *   pincode: not exactly 6 digits                      -> "PIN code needs 6 digits"
 *   qty: not a whole number from 1 to 10               -> "Quantity must be 1 to 10"
 */
export function validateOrder(form: OrderForm): FormErrors<OrderForm> {
  const errors: FormErrors<OrderForm> = {};
  if (form.name.trim() === "") {
    errors.name = "Please enter your name";
  }
  if (!/^\d{10}$/.test(form.phone.replace(/\s/g, ""))) {
    errors.phone = "Phone needs 10 digits";
  }
  if (!/^\d{6}$/.test(form.pincode)) {
    errors.pincode = "PIN code needs 6 digits";
  }
  if (!Number.isInteger(form.qty) || form.qty < 1 || form.qty > 10) {
    errors.qty = "Quantity must be 1 to 10";
  }
  return errors;
}

/** true when errors holds at least one message. */
export function hasErrors<T>(errors: FormErrors<T>): boolean {
  return Object.values(errors).some((message) => typeof message === "string");
}
