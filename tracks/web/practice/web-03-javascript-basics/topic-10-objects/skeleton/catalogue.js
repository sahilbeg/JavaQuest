// The price list as one object: each code points to the item's name and price in paise.

export const CATALOGUE = {
  CST: { name: "Coaster set (4)", paise: 15000 },
  KEY: { name: "Keychain", paise: 12000 },
  AMI: { name: "Amigurumi", paise: 45000 },
  BAG: { name: "Tote bag", paise: 89900 },
  // TODO 1: add BTY, the "Baby booties", at 35000 paise
};

export function priceOf(code) {
  // TODO 2: look up code in CATALOGUE with square brackets. Return its paise, or null when the code is not there.
  return null;
}

export function makeLine(code, qty) {
  // TODO 3: return a cart line like { code: "AMI", name: "Amigurumi", paise: 45000, qty: 2 }.
  // For a code that is not in CATALOGUE, return null.
  return null;
}

export function cartTotal(lines) {
  // TODO 4: add up paise * qty for every line object in the lines array
  return 0;
}
