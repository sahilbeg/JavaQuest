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
  if (form.name.trim() === "") errors.name = "Please enter your name.";
  if (!/^\d{10}$/.test(form.phone.replace(/ /g, ""))) errors.phone = "Phone needs 10 digits, like 98765 43210.";
  const qty = form.qty.trim();
  if (!/^\d+$/.test(qty) || Number(qty) < 1 || Number(qty) > 20) errors.qty = "Order between 1 and 20.";
  if (form.stitchedName.trim().length > 12) errors.stitchedName = "Names can be up to 12 letters.";
  return errors;
}

/** The clean request, for a form that has passed validation. */
export function toRequest(form: RequestForm): CustomRequest {
  const stitched = form.stitchedName.trim();
  return {
    name: form.name.trim(),
    phone: form.phone.replace(/ /g, ""),
    colour: form.colour,
    qty: Number(form.qty.trim()),
    stitchedName: stitched === "" ? null : stitched,
  };
}
