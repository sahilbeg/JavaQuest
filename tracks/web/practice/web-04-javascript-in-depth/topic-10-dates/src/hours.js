// Opening hours for the Aundh shop and the FC Road stall. Every function takes the date in; none reads the clock.

export const DAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
export const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

function minutesAfterMidnight(date) {
  return date.getHours() * 60 + date.getMinutes();
}

export function isShopOpen(date) {
  // TODO 1: closed on Monday (getDay() is 1). Otherwise open from 11:00 (11 * 60) up to, not including, 21:00.
  return false;
}

export function isStallOpen(date) {
  // TODO 2: open only on Saturday (6) and Sunday (0), from 10:00 up to, not including, 20:00.
  return false;
}

export function addDays(date, days) {
  // TODO 3: return a new Date with days added to getDate(), keeping the year, month, hours and minutes
  return date;
}

export function formatDay(date) {
  // TODO 4: return text like "Sat 31 Oct" using DAYS, getDate() and MONTHS
  return "";
}
