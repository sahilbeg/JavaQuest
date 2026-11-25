// Dhaaga's fixed prices and fees. Every amount is whole paise: ₹1 is 100 paise.

export const ITEM_CODES = ["CST", "KEY", "AMI", "BAG", "BTY"] as const;
export type ItemCode = (typeof ITEM_CODES)[number];

export const PRICES = { CST: 15000, KEY: 12000, AMI: 45000, BAG: 89900, BTY: 35000 } as const;

export const GIFT_WRAP = 3000; // ₹30 per item
export const NAME_STITCHED = 5000; // ₹50 per item
export const DELIVERY_FEE = 6000; // ₹60 inside Pune
export const FREE_DELIVERY_ABOVE = 99900; // free when the subtotal is above ₹999
export const DIWALI_ABOVE = 150000; // 10% off when the subtotal is above ₹1,500
