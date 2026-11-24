// Turning item codes into prices and names for the order screen.

export function priceOf(code) {
  // TODO 1: use a switch on code to return the price in paise:
  // CST 15000, KEY 12000, AMI 45000, BAG 89900, BTY 35000. Any other code gives null.
  return null;
}

export function itemName(code) {
  // TODO 2: use a switch to return "Coaster set (4)", "Keychain", "Amigurumi", "Tote bag" or "Baby booties".
  // Any other code gives "Unknown item".
  return "";
}

export function isWeekend(day) {
  // TODO 3: use a switch where "Sat" and "Sun" share one case that returns true. Every other day gives false.
  return false;
}

export function itemsText(count) {
  // TODO 4: use the ternary operator: 1 gives "1 item", any other count gives "<count> items", like "3 items"
  return "";
}
