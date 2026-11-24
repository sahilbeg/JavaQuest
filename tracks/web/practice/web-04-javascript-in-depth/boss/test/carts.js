// Fresh carts for the checks. Each call builds a new object, so no check can spoil another.

function cart(id, lines) {
  const totalPaise = lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  return { id, lines, totalPaise };
}

/** Mrs. Rao: two tote bags and a coaster set, ₹1,948.00. */
export function raoCart() {
  return cart("order-17", [
    { code: "BAG", paise: 89900, qty: 2 },
    { code: "CST", paise: 15000, qty: 1 },
  ]);
}

/** Three lines: a tote bag, two amigurumi and a keychain, ₹1,919.00. */
export function threeLineCart() {
  return cart("order-18", [
    { code: "BAG", paise: 89900, qty: 1 },
    { code: "AMI", paise: 45000, qty: 2 },
    { code: "KEY", paise: 12000, qty: 1 },
  ]);
}

/** One line above the limit: two tote bags, ₹1,798.00. */
export function twoBagsCart() {
  return cart("order-19", [{ code: "BAG", paise: 89900, qty: 2 }]);
}

/** Below the limit: two amigurumi and baby booties, ₹1,250.00. */
export function smallCart() {
  return cart("order-20", [
    { code: "AMI", paise: 45000, qty: 2 },
    { code: "BTY", paise: 35000, qty: 1 },
  ]);
}

/** Exactly at the limit: ten coaster sets, ₹1,500.00. */
export function exactlyLimitCart() {
  return cart("order-21", [{ code: "CST", paise: 15000, qty: 10 }]);
}

/** Just above the limit, and below it once discounted: ₹1,589.00 becomes ₹1,430.10. */
export function nearLimitCart() {
  return cart("order-22", [
    { code: "BAG", paise: 89900, qty: 1 },
    { code: "AMI", paise: 45000, qty: 1 },
    { code: "KEY", paise: 12000, qty: 2 },
  ]);
}
