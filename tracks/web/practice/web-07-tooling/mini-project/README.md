# FEAT-07: One toolbox for prices, links and PIN codes

Build Dhaaga's small utilities package: four tested functions, and one front door that exports them.

## src/money.js

**`formatRupees(paise)`**: whole paise to rupees text, with `new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" })`.

| paise | result |
|---|---|
| `15000` | `"₹150.00"` |
| `129900` | `"₹1,299.00"` |
| `5` | `"₹0.05"` |
| `15000000` | `"₹1,50,000.00"` |

Anything that is not a whole number (`12.5`, `"15000"`) throws a `TypeError`.

**`paiseFromRupees(text)`**: typed rupees to whole paise, without floating point.

- Spaces around the text, a leading `₹` (with or without a space after it) and commas are allowed.
- Then it must be digits, optionally followed by `.` and one or two digits.
- `"450"`, `"₹450"`, `"₹ 450"` give `45000`. `"₹1,299.50"`, `"1299.5"` give `129950`. `"0.05"` gives `5`.
- Text that is not an amount (`""`, `"abc"`, `"-5"`, `"12.345"`, `"₹"`) throws a `RangeError`.
- Anything that is not text throws a `TypeError`.

## src/text.js

**`slugify(name)`**: a web-address name, in small letters, digits and single dashes.

- Accents are removed (`"Café"` becomes `"cafe"`).
- Every run of other characters becomes one `-`, with no `-` at the start or end.
- `"Golu the Elephant!"` gives `"golu-the-elephant"`. `"!!!"` gives `""`.

## src/pin.js

**`isPunePin(pin)`**: `true` for six digits starting with `411`, like `"411038"`.

- Spaces around it are ignored, and one space after the third digit is allowed: `"411 038"`.
- Anything else is `false`, including numbers like `411038`, five or seven digits, and letters.

## src/index.js

Export exactly `formatRupees`, `paiseFromRupees`, `slugify` and `isPunePin`, and nothing else.
