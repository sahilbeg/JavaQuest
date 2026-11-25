# Typed events

Raju types item codes into the counter screen in `CounterPad.tsx`. Make it quick to use.

1. **TODO 1:** Finish `addTyped()`. Trim the text, then:
   - empty: set the message to `Type a code first`
   - a real code (check with `isItemCode`): call `onAdd(code)`, set the message to `Added Amigurumi` (the name comes from `ITEM_NAMES`), and empty the box
   - anything else: set the message to `Unknown code: XYZ`, and leave the text in the box
2. **TODO 2:** Write `handleChange(e: ChangeEvent<HTMLInputElement>)`, which keeps the typed text in capitals: typing `ami` shows `AMI`.
3. **TODO 3:** Write `handleKeyDown(e: KeyboardEvent<HTMLInputElement>)`, which runs `addTyped()` when the key is Enter.
4. **TODO 4:** Use both handlers on the input.

The **Add** button already runs `addTyped`. The codes are `CST`, `KEY`, `AMI`, `BAG` and `BTY` (see `items.ts`). Import the event types from `"react"`; the imports are already there. Press **Submit** to run the checks.
