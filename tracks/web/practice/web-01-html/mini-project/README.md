# FEAT-01: The custom-order request form

Build the fields inside the `<form>` in `index.html`. Put each field in its own `<p>` with a `<label>` joined by `for` and `id`.

1. **TODO 1:** `Your name`: a required text input, `id="name"`, `name="name"`.
2. **TODO 2:** `Phone number`: a required `type="tel"` input, `id="phone"`, `name="phone"`, with a `pattern` for exactly ten digits.
3. **TODO 3:** `Item`: a required `<select id="item" name="item">`. Options in order: `value=""` Choose an item, then `CST` Coaster set (4), `KEY` Keychain, `AMI` Amigurumi (small toy), `BAG` Tote bag, `BTY` Baby booties.
4. **TODO 4:** a `<fieldset>` with `<legend>Colour</legend>` and five radio buttons, all `name="colour"`, each with its own label. Values in order: `marigold`, `indigo`, `rani-pink`, `mint`, `off-white`. Put `required` on the first one.
5. **TODO 5:** `How many?`: `type="number"`, `id="qty"`, `name="qty"`, `min="1"`, `max="20"`, `value="1"`.
6. **TODO 6:** `Notes`: a `<textarea id="notes" name="notes" rows="5">`, not required.
7. **TODO 7:** `<button type="submit">Send request</button>`.

The labels must say exactly `Your name`, `Phone number`, `Item`, `How many?` and `Notes`. Try pressing **Send request** in the preview with a field missing: the browser should stop you.
