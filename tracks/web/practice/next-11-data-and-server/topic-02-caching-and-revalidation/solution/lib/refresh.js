import { revalidatePath, revalidateTag } from "next/cache";

/** Called by the admin page right after Meera Aunty changes an item's price. */
export function afterPriceChange(itemId) {
  revalidateTag("catalogue", "max");
  revalidatePath(`/items/${itemId}`);
}

/** Called by the admin page right after the opening hours change. */
export function afterHoursChange() {
  revalidateTag("hours", "max");
}
