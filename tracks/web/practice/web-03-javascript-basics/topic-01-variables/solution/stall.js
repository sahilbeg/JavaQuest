// The FC Road stall counter.

export const SHOP_NAME = "Dhaaga & Co.";

export const CITY = "Pune";

export function coastersAfterSunday() {
  let coasters = 12;
  coasters = coasters - 3;
  coasters = coasters + 5;
  coasters = coasters - 4;
  return coasters;
}

export function welcomeLine(name) {
  return "Welcome to " + SHOP_NAME + ", " + name + "!";
}
