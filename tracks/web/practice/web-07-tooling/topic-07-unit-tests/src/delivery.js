// Delivery inside Pune, in paise. ₹60 = 6000 paise. Free above ₹999 = 99900 paise.
export const DELIVERY_PAISE = 6000;
export const FREE_ABOVE_PAISE = 99900;

/**
 * The delivery fee for an order whose items cost subtotalPaise.
 * An empty cart pays 0. Up to and including ₹999 pays DELIVERY_PAISE. Above ₹999 is free.
 */
export function deliveryFee(subtotalPaise) {
  // TODO 1: throw a RangeError when subtotalPaise is not a whole number (Number.isInteger) or is below 0.
  // TODO 2: return 0 for an empty cart, 0 above FREE_ABOVE_PAISE, and DELIVERY_PAISE otherwise.
  return 0;
}
