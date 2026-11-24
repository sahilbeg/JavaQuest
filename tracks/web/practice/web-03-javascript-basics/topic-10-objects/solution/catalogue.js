// The price list as one object: each code points to the item's name and price in paise.

export const CATALOGUE = {
  CST: { name: "Coaster set (4)", paise: 15000 },
  KEY: { name: "Keychain", paise: 12000 },
  AMI: { name: "Amigurumi", paise: 45000 },
  BAG: { name: "Tote bag", paise: 89900 },
  BTY: { name: "Baby booties", paise: 35000 },
};

export function priceOf(code) {
  const item = CATALOGUE[code];
  if (item === undefined) {
    return null;
  }
  return item.paise;
}

export function makeLine(code, qty) {
  const item = CATALOGUE[code];
  if (item === undefined) {
    return null;
  }
  return { code, name: item.name, paise: item.paise, qty };
}

export function cartTotal(lines) {
  let total = 0;
  for (const line of lines) {
    total = total + line.paise * line.qty;
  }
  return total;
}
