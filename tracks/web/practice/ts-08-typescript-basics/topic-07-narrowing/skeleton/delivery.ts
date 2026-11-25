// Delivery details arrive in different shapes. Check which shape you have before you use it.

export function pinText(pin: string | number): string {
  // TODO 1: a number like 411001 becomes "411001". Text like "411 001" loses its spaces: "411001".
  return "";
}

export function contactLine(contact: { phone: string } | { email: string }): string {
  // TODO 2: "Phone: 98765 43210" for a phone contact, "Email: asha@example.com" for an email contact
  return "";
}

export function itemCount(items: string | string[]): number {
  // TODO 3: a list of item codes counts its length; a single code on its own counts as 1
  return 0;
}

export function pickupDate(when: Date | string): string {
  // TODO 4: a Date becomes its "YYYY-MM-DD" date (use toISOString); text is already a date and stays as it is
  return "";
}

export function giftNote(message: string | null): string {
  // TODO 5: "Note: " and the message, trimmed. null, or a message that is empty after trimming, gives "No note".
  return "";
}
