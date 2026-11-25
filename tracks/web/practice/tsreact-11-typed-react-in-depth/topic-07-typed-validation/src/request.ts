export type Colour = "Marigold" | "Indigo" | "Rani pink" | "Mint" | "Off-white";

export const COLOURS: readonly Colour[] = ["Marigold", "Indigo", "Rani pink", "Mint", "Off-white"];

/** True when a string from a <select> is one of Dhaaga's colours. */
export function isColour(value: string): value is Colour {
  return (COLOURS as readonly string[]).includes(value);
}

/** What the form holds while the customer types: the boxes all hold text. */
export interface RequestForm {
  name: string;
  phone: string;
  colour: Colour;
  qty: string;
  stitchedName: string;
}

/** At most one message per field. A field with no message is fine. */
export type RequestErrors = Partial<Record<keyof RequestForm, string>>;

/** The clean request Meera Aunty gets. */
export interface CustomRequest {
  name: string;
  phone: string;
  colour: Colour;
  qty: number;
  stitchedName: string | null;
}

export const EMPTY_FORM: RequestForm = { name: "", phone: "", colour: "Marigold", qty: "1", stitchedName: "" };

/** One message per bad field, or {} when the form is fine. */
export function validateRequest(form: RequestForm): RequestErrors {
  const errors: RequestErrors = {};
  // TODO 1: add a message for each bad field:
  //   name empty after trim                      -> "Please enter your name."
  //   phone without spaces isn't exactly 10 digits -> "Phone needs 10 digits, like 98765 43210."
  //   qty (trimmed) isn't whole digits, or < 1, or > 20 -> "Order between 1 and 20."
  //   stitchedName longer than 12 after trim     -> "Names can be up to 12 letters."
  return errors;
}

/** The clean request, for a form that has passed validation. */
export function toRequest(form: RequestForm): CustomRequest {
  // TODO 2: trim the name, remove the spaces from the phone, turn qty into a number,
  //         and trim stitchedName, using null when it is empty.
  return { name: "", phone: "", colour: form.colour, qty: 0, stitchedName: null };
}
