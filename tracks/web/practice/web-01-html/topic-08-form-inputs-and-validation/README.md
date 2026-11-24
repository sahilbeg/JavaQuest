# Input types and built-in validation

Make the quick order form stop bad orders before they're sent. Change the form in `index.html`.

1. **TODO 1:** make the name input `required`.
2. **TODO 2:** make the phone input `type="tel"`, `required`, with a `pattern` that accepts exactly ten digits and nothing else.
3. **TODO 3:** make the email input `type="email"`. It stays optional, so don't make it required.
4. **TODO 4:** under the `Item` label, add a required `<select id="order-item" name="item">`. Its options, in order: `value=""` saying `Choose an item`, then `CST` Coaster set (4), `KEY` Keychain, `AMI` Amigurumi (small toy), `BAG` Tote bag, `BTY` Baby booties.
5. **TODO 5:** under the `How many?` label, add `<input type="number" id="order-qty" name="qty">` with `min="1"`, `max="10"` and `value="1"`.
6. **TODO 6:** add a `<fieldset>` with `<legend>Delivery</legend>` and two radio buttons, both with `name="delivery"`:
   - `id="delivery-pickup"`, `value="pickup"`, label `Pick up at the Aundh shop`
   - `id="delivery-pune"`, `value="pune"`, label `Deliver in Pune (₹60, free above ₹999)`

Try it in the preview: press **Send order** with the name empty, or with a five-digit phone number. The browser stops you.
