// Paise maths for the stall. Every amount is whole paise: ₹1 is 100 paise.

export function lineTotal(paise, qty) {
  return paise * qty;
}

export function wholeRupees(paise) {
  return Math.floor(paise / 100);
}

export function leftoverPaise(paise) {
  return paise % 100;
}

export function shareEach(totalPaise, people) {
  return Math.floor(totalPaise / people);
}
