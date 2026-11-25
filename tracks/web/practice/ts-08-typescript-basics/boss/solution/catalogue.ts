// Prices come from Meera Aunty's price sheet. Every amount in the shop is whole paise: ₹1 is 100 paise.

export interface CatalogueRow {
  code: string;
  name: string;
  price: number | string; // the sheet sends some prices as text
}

export interface CartLine {
  code: string;
  qty: number;
  giftWrap?: boolean;
}

export const GIFT_WRAP = 3000; // ₹30 per item

export const ROWS: CatalogueRow[] = [
  { code: "CST", name: "Coaster set (4)", price: 15000 },
  { code: "KEY", name: "Keychain", price: "12000" },
  { code: "AMI", name: "Amigurumi", price: 45000 },
  { code: "BAG", name: "Tote bag", price: "89900" },
  { code: "BTY", name: "Baby booties", price: " 35000 " },
];

export function toPaise(price: number | string): number {
  let paise: number;
  if (typeof price === "number") {
    paise = price;
  } else {
    const text = price.trim();
    paise = text === "" ? NaN : Number(text);
  }
  if (!Number.isInteger(paise) || paise < 0) {
    throw new Error(`Bad price: ${price}`);
  }
  return paise;
}

export function lineTotal(line: CartLine, rows: CatalogueRow[] = ROWS): number {
  const row = rows.find((r) => r.code === line.code);
  if (!row) {
    throw new Error(`Unknown item: ${line.code}`);
  }
  const unit = toPaise(row.price) + (line.giftWrap ? GIFT_WRAP : 0);
  return unit * line.qty;
}

export function cartTotal(lines: CartLine[], rows: CatalogueRow[] = ROWS): number {
  let total = 0;
  for (const line of lines) {
    total += lineTotal(line, rows);
  }
  return total;
}

export function rupeesToPaise(text: string): number {
  const cleaned = text.replaceAll("₹", "").replaceAll(",", "").trim();
  const rupees = cleaned === "" ? NaN : Number(cleaned);
  if (!Number.isFinite(rupees) || rupees < 0) {
    throw new Error(`Bad price: ${text}`);
  }
  return Math.round(rupees * 100);
}
