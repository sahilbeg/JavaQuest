// Turning item codes into prices and names for the order screen.

export function priceOf(code) {
  switch (code) {
    case "CST":
      return 15000;
    case "KEY":
      return 12000;
    case "AMI":
      return 45000;
    case "BAG":
      return 89900;
    case "BTY":
      return 35000;
    default:
      return null;
  }
}

export function itemName(code) {
  switch (code) {
    case "CST":
      return "Coaster set (4)";
    case "KEY":
      return "Keychain";
    case "AMI":
      return "Amigurumi";
    case "BAG":
      return "Tote bag";
    case "BTY":
      return "Baby booties";
    default:
      return "Unknown item";
  }
}

export function isWeekend(day) {
  switch (day) {
    case "Sat":
    case "Sun":
      return true;
    default:
      return false;
  }
}

export function itemsText(count) {
  return `${count} ${count === 1 ? "item" : "items"}`;
}
