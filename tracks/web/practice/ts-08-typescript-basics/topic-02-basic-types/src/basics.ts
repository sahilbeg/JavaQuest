// The first TypeScript file of the price calculator. Every amount is whole paise: ₹1 is 100 paise.

export const SHOP_NAME: string = "Dhaaga & Co.";
export const TOY_GRAMS: number = 40; // a small amigurumi uses about 40 g of yarn

export function toPaise(rupees: number): number {
  // TODO 1: turn rupees into whole paise, rounded to the nearest paisa: 450 gives 45000, 0.29 gives 29
  return 0;
}

export function parseQty(typed: string): number {
  // TODO 2: turn what a customer typed into a number of items, ignoring spaces around it.
  //         Anything that isn't a whole number of 0 or more ("two", "1.5", "-1") gives 0.
  return 0;
}

export function rupeesText(paise: number): string {
  // TODO 3: show paise as rupees with two decimals after a ₹ sign: 45000 gives "₹450.00"
  return "";
}

export function canMakeToys(grams: number, toys: number): boolean {
  // TODO 4: true when grams of yarn is enough for that many toys, at TOY_GRAMS each
  return false;
}

export function stockLabel(name: string, left: number): string {
  // TODO 5: "Keychain: 3 left", or "Keychain: sold out" when none are left
  return "";
}
