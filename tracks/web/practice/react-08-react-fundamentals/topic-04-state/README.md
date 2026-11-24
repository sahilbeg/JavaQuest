# State with useState

The quantity picker on the item page never moves past 1. Fix it in `QuantityPicker.jsx`. The picker gets three props: `name`, `paise` (the price of one) and `stock` (how many are left).

1. **TODO 1:** Replace `let qty = 1;` with state that starts at 1: `const [qty, setQty] = useState(1);`
2. **TODO 2:** The `-` button (labelled "One less") sets `qty` to one less. It is disabled while `qty` is 1.
3. **TODO 3:** The `+` button (labelled "One more") sets `qty` to one more. It is disabled when `qty` equals `stock`.
4. **TODO 4:** `p.line-total` shows the price of `qty` items: three coaster sets show `₹450.00`.

Use the setter for every change: assigning to `qty` never redraws the page. Keep the `aria-label`s, because the checks find the buttons by them.

The preview shows two pickers. Check that each keeps its own count. Press **Submit** to run the checks.
