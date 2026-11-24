// Your table of test cases for deliveryFee. The checks run every case against the correct fee,
// against your own deliveryFee, and against five broken versions (see the README).
// Each case: { name: "a sentence", subtotal: <whole paise>, expected: <fee in paise> }

export const cases = [
  { name: "an empty cart pays nothing", subtotal: 0, expected: 0 },
  { name: "a ₹150 coaster set pays ₹60", subtotal: 15000, expected: 6000 },
  { name: "exactly ₹999 still pays ₹60", subtotal: 99900, expected: 6000 },
  { name: "one paisa above ₹999 is free", subtotal: 99901, expected: 0 },
  { name: "two tote bags are free", subtotal: 179800, expected: 0 },
];
