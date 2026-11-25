# INC-08: A price arrives as text and the total breaks

The price sheet sends some prices as text, like `"12000"`, and `toPaise` in `src/catalogue.ts` tells TypeScript to trust that they're numbers. Then `+` glues instead of adding. Make every price a real number before it's used.

1. **Bronze:** `toPaise(price)` checks which type it got. A number stays as it is. Text is trimmed and turned into a number: `" 35000 "` gives `35000`. `toPaise` must always give back a real `number`. Then a keychain is `12000` paise again, and Mrs. Deshpande's keychain and coaster set come to `27000`.
2. **Silver:** `toPaise` throws `new Error("Bad price: ...")` for anything that isn't a whole number of paise, 0 or more: `"abc"`, `""`, `"   "`, `"120.50"`, `"-100"`, `"45O00"` (a letter O) or `150.5`. `lineTotal` throws `new Error("Unknown item: XYZ")` for a code that isn't in the rows, instead of charging 0.
3. **Gold:** `rupeesToPaise(text)` reads Meera Aunty's hand-typed rupee column. It ignores `₹`, commas and spaces, and rounds to the nearest paisa: `"₹1,299.50"` gives `129950`. Text with no amount in it, or a negative amount, throws `"Bad price: ..."`.

Press **Submit** to run the checks.
