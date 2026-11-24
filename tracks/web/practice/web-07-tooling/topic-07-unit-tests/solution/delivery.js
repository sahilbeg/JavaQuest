// Delivery inside Pune, in paise. ₹60 = 6000 paise. Free above ₹999 = 99900 paise.
export const DELIVERY_PAISE = 6000;
export const FREE_ABOVE_PAISE = 99900;

/**
 * The delivery fee for an order whose items cost subtotalPaise.
 * An empty cart pays 0. Up to and including ₹999 pays DELIVERY_PAISE. Above ₹999 is free.
 */
export function deliveryFee(subtotalPaise) {
  if (!Number.isInteger(subtotalPaise) || subtotalPaise < 0) {
    throw new RangeError(`A subtotal must be whole paise, 0 or more, not ${subtotalPaise}`);
  }
  if (subtotalPaise === 0 || subtotalPaise > FREE_ABOVE_PAISE) {
    return 0;
  }
  return DELIVERY_PAISE;
}
