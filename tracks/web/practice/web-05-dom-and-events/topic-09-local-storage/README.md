# Remembering things: localStorage

Make the site remember Mrs. Rao's PIN code and her saved-for-later hearts, in `src/prefs.js`. Use the two key constants at the top of the file.

1. **TODO 1:** `savePin(pin)` saves the PIN under `dhaaga:pin`.
2. **TODO 2:** `loadPin()` returns the saved PIN, or `""` when none is saved.
3. **TODO 3:** `saveWishlist(ids)` saves the array of item ids under `dhaaga:wishlist`, as JSON text like `["DC-1042","DC-1013"]`.
4. **TODO 4:** `loadWishlist()` returns the saved array. It returns `[]`, and never throws, when nothing is saved, when the text is broken JSON, or when it is JSON but not an array.
5. **TODO 5:** `toggleWish(id)` adds the id to the end of the saved wishlist, or removes it if it is already there. It saves the result and returns the new array.
6. **TODO 6:** `forgetMe()` forgets the PIN and the wishlist, and leaves every other key alone.

Open the preview, type a PIN, tap some hearts, then reload the preview: they should still be there.

Press **Submit** in Cadence, or save the file in your IDE. The checks run on their own.
