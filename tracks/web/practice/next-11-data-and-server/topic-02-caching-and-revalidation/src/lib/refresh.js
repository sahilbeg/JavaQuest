import { revalidatePath, revalidateTag } from "next/cache";

/** Called by the admin page right after Meera Aunty changes an item's price. */
export function afterPriceChange(itemId) {
  // TODO 4: mark everything tagged "catalogue" as stale (profile "max"),
  // and refresh the item's own page, /items/<itemId>.
}

/** Called by the admin page right after the opening hours change. */
export function afterHoursChange() {
  // TODO 5: mark everything tagged "hours" as stale (profile "max").
}
