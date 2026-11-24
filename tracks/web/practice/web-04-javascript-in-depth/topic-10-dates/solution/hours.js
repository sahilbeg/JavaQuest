// Opening hours for the Aundh shop and the FC Road stall. Every function takes the date in; none reads the clock.

export const DAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
export const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

function minutesAfterMidnight(date) {
  return date.getHours() * 60 + date.getMinutes();
}

export function isShopOpen(date) {
  const minutes = minutesAfterMidnight(date);
  return date.getDay() !== 1 && minutes >= 11 * 60 && minutes < 21 * 60;
}

export function isStallOpen(date) {
  const day = date.getDay();
  const minutes = minutesAfterMidnight(date);
  return (day === 6 || day === 0) && minutes >= 10 * 60 && minutes < 20 * 60;
}

export function addDays(date, days) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate() + days, date.getHours(), date.getMinutes());
}

export function formatDay(date) {
  return DAYS[date.getDay()] + " " + date.getDate() + " " + MONTHS[date.getMonth()];
}
