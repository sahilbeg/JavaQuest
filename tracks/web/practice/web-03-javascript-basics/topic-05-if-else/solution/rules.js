// Rules the website follows. Days are "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" and "Sun". Hours are 0 to 23.

export function deliveryFee(subtotalPaise, pickup) {
  if (pickup) {
    return 0;
  }
  if (subtotalPaise > 99900) {
    return 0;
  }
  return 6000;
}

export function stallOpen(day, hour) {
  const weekend = day === "Sat" || day === "Sun";
  return weekend && hour >= 10 && hour < 20;
}

export function noteLine(note) {
  if (note) {
    return "Note: " + note;
  }
  return "No note";
}

export function priceBand(paise) {
  if (paise < 20000) {
    return "under ₹200";
  } else if (paise < 50000) {
    return "₹200 to ₹499";
  } else {
    return "₹500 and up";
  }
}
