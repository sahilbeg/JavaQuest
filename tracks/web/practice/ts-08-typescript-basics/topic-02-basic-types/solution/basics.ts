// The first TypeScript file of the price calculator. Every amount is whole paise: ₹1 is 100 paise.

export const SHOP_NAME: string = "Dhaaga & Co.";
export const TOY_GRAMS: number = 40; // a small amigurumi uses about 40 g of yarn

export function toPaise(rupees: number): number {
  return Math.round(rupees * 100);
}

export function parseQty(typed: string): number {
  const qty = Number(typed.trim());
  return Number.isInteger(qty) && qty >= 0 ? qty : 0;
}

export function rupeesText(paise: number): string {
  return "₹" + (paise / 100).toFixed(2);
}

export function canMakeToys(grams: number, toys: number): boolean {
  return grams >= toys * TOY_GRAMS;
}

export function stockLabel(name: string, left: number): string {
  return left === 0 ? `${name}: sold out` : `${name}: ${left} left`;
}
