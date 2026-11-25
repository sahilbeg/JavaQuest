import { getItem } from "./store.js";

// The cart lives in a cookie called "cart", written like "DC-1042:2,DC-1101:1" (id:qty, separated by commas).
export const CART_COOKIE = "cart";

/** The cookie value after adding one more of an item, at most 5: "DC-1042:2" plus DC-1042 gives "DC-1042:3". */
export function addToCartValue(value, id) {
  const entries = value ? value.split(",") : [];
  let found = false;
  const next = entries.map((entry) => {
    const [entryId, qty] = entry.split(":");
    if (entryId !== id) return entry;
    found = true;
    return `${entryId}:${Math.min(Number(qty) + 1, 5)}`;
  });
  if (!found) next.push(`${id}:1`);
  return next.join(",");
}

/**
 * The cart's lines, read from a cookie store like the one Next.js's cookies() gives:
 * cookieStore.get("cart") returns { name, value }, or undefined when there's no cart cookie.
 */
export async function readCart(cookieStore) {
  const value = cookieStore.get(CART_COOKIE)?.value ?? "";
  const lines = [];
  for (const entry of value.split(",")) {
    const [id, qtyText = ""] = entry.split(":");
    const item = await getItem(id);
    const qty = Number(qtyText);
    if (item && /^\d+$/.test(qtyText) && qty >= 1 && qty <= 5) {
      lines.push({ id: item.id, name: item.name, paise: item.paise, qty });
    }
  }
  return lines;
}
