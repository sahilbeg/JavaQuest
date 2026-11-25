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
  // TODO 1: test text against a regular expression.
  return false;
}

/** A new object with the same keys as obj, where each value is fn(value, key). obj is not changed. */
export function mapValues<T extends object, R>(
  obj: T,
  fn: (value: T[keyof T], key: keyof T) => R,
): { [K in keyof T]: R } {
  // TODO 2: loop over Object.keys(obj) as (keyof T)[] and fill a new object.
  return {} as { [K in keyof T]: R };
}

/**
 * The problems with the form, one message per bad field. {} when everything is fine.
 *   name: empty or only spaces                         -> "Please enter your name"
 *   phone: not exactly 10 digits once spaces are removed -> "Phone needs 10 digits"
 *   pincode: not exactly 6 digits                      -> "PIN code needs 6 digits"
 *   qty: not a whole number from 1 to 10               -> "Quantity must be 1 to 10"
 */
export function validateOrder(form: OrderForm): FormErrors<OrderForm> {
  // TODO 3: start with an empty FormErrors<OrderForm> and add a message for each bad field.
  return {};
}

/** true when errors holds at least one message. */
export function hasErrors<T>(errors: FormErrors<T>): boolean {
  // TODO 4: look for any value that is a string.
  return false;
}
