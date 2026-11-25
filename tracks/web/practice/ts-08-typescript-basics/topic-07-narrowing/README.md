# Narrowing: checking before you use

Delivery details reach `src/delivery.ts` in different shapes. Each function takes a union: check which member you have, then use it.

1. **TODO 1:** `pinText(pin)`: a number like `411001` becomes `"411001"`. Text like `"411 001"` loses its spaces. Use `typeof`.
2. **TODO 2:** `contactLine(contact)`: `"Phone: 98765 43210"` for `{ phone }`, and `"Email: asha@example.com"` for `{ email }`. Use `in`.
3. **TODO 3:** `itemCount(items)`: a list counts its length, and a single code counts as `1`. Use `Array.isArray`.
4. **TODO 4:** `pickupDate(when)`: a `Date` becomes `"YYYY-MM-DD"` with `toISOString().slice(0, 10)`, and text stays as it is. Use `instanceof`.
5. **TODO 5:** `giftNote(message)`: `"Note: "` and the trimmed message. `null`, or a message that's empty after trimming, gives `"No note"`.

Press **Submit** to run the checks.
