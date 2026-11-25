// Delivery details arrive in different shapes. Check which shape you have before you use it.

export function pinText(pin: string | number): string {
  if (typeof pin === "number") {
    return String(pin);
  }
  return pin.replaceAll(" ", "");
}

export function contactLine(contact: { phone: string } | { email: string }): string {
  if ("phone" in contact) {
    return `Phone: ${contact.phone}`;
  }
  return `Email: ${contact.email}`;
}

export function itemCount(items: string | string[]): number {
  return Array.isArray(items) ? items.length : 1;
}

export function pickupDate(when: Date | string): string {
  if (when instanceof Date) {
    return when.toISOString().slice(0, 10);
  }
  return when;
}

export function giftNote(message: string | null): string {
  if (message === null) {
    return "No note";
  }
  const text = message.trim();
  return text ? `Note: ${text}` : "No note";
}
