// Text for tags, WhatsApp messages and stitched names.

export function itemLabel(code, name) {
  return `${code}: ${name}`;
}

export function readyMessage(name, orderId) {
  return `Hi ${name}, ${orderId} is ready at the FC Road stall.`;
}

export function cleanCode(typed) {
  return typed.trim().toUpperCase();
}

export function initials(first, last) {
  return (first[0] + last[0]).toUpperCase();
}
