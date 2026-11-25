/** Paise to rupees for the page: 129900 becomes "₹1,299.00". */
export function rupees(paise: number): string {
  const whole = String(Math.floor(paise / 100)).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  return `₹${whole}.${String(paise % 100).padStart(2, "0")}`;
}
