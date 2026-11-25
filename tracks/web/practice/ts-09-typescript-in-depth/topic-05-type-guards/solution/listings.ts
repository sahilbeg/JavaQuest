// The price list holds single products and gift bundles. Type guards tell them apart.

export interface Product {
  id: string;
  name: string;
  paise: number;
}

export interface Bundle {
  id: string;
  name: string;
  itemIds: string[];
  bundlePaise: number;
}

export type Listing = Product | Bundle;

/** true when the listing is a bundle: bundles are the listings that have itemIds. */
export function isBundle(listing: Listing): listing is Bundle {
  return "itemIds" in listing;
}

/** What the customer pays: paise for a product, bundlePaise for a bundle. */
export function priceOf(listing: Listing): number {
  return isBundle(listing) ? listing.bundlePaise : listing.paise;
}

/** Only the bundles, in their original order. */
export function bundlesOnly(listings: Listing[]): Bundle[] {
  return listings.filter(isBundle);
}

/** true for any value that isn't null or undefined. 0 and "" are present. Works with filter: list.filter(isPresent). */
export function isPresent<T>(value: T | null | undefined): value is T {
  return value !== null && value !== undefined;
}

/**
 * A message to show for anything that was thrown or rejected:
 * an Error gives its message, a string is shown as it is, anything else gives "Something went wrong".
 */
export function messageFor(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  if (typeof error === "string") {
    return error;
  }
  return "Something went wrong";
}
