# Custom properties, hover and focus

Keep each brand colour in one place, and make the buttons and links react to the mouse and the keyboard.

1. **TODO 1:** add a `:root` rule with three custom properties:
   - `--marigold: #f2a81d`
   - `--indigo: #2e3a8c`
   - `--off-white: #faf7f0`
2. **TODO 2:** replace every brand hex code below `:root` with `var(--marigold)`, `var(--indigo)` or `var(--off-white)`. Leave the greys (`#5a5a5a`, `rgb(90, 90, 90)`) and the red (`#b3261e`) as they are.
3. **TODO 3:** add `.buy:hover` with `background-color: var(--indigo)` and `color: var(--off-white)`.
4. **TODO 4:** add `.buy:focus-visible` with `outline: 3px solid var(--indigo)` and `outline-offset: 2px`.
5. **TODO 5:** add one grouped rule for `nav a:hover, nav a:focus-visible` with `text-decoration: underline`.

Name the custom properties exactly as above, in small letters. Never set `outline: none`.

Point at a button in the preview, then press Tab to move through the links.

File to change: `style.css`.
