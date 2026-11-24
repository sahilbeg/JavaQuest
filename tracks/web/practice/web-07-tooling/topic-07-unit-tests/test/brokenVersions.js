// The correct delivery fee, and five broken versions of it ("mutants").
// Good test cases pass on correctFee and fail on every broken version.

export function correctFee(subtotal) {
  if (subtotal === 0) return 0;
  return subtotal > 99900 ? 0 : 6000;
}

export const brokenVersions = [
  {
    description: "is free from exactly ₹999 (>= instead of >)",
    fee: (subtotal) => (subtotal === 0 ? 0 : subtotal >= 99900 ? 0 : 6000),
  },
  {
    description: "charges 60 paise instead of 6000",
    fee: (subtotal) => (subtotal === 0 ? 0 : subtotal > 99900 ? 0 : 60),
  },
  {
    description: "is never free",
    fee: (subtotal) => (subtotal === 0 ? 0 : 6000),
  },
  {
    description: "charges ₹60 for an empty cart",
    fee: (subtotal) => (subtotal > 99900 ? 0 : 6000),
  },
  {
    description: "is free above ₹99.90 (a zero missing from 99900)",
    fee: (subtotal) => (subtotal === 0 ? 0 : subtotal > 9990 ? 0 : 6000),
  },
];
