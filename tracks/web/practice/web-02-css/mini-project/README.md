# FEAT-02: A festive Diwali theme

Give the catalogue a Diwali look that one class switches on and off.

| Token | Everyday (on `:root`) | Diwali (on `.theme-diwali`) |
|---|---|---|
| `--brand` | `#2e3a8c` | `#8c1d40` |
| `--accent` | `#f2a81d` | `#ffc93c` |
| `--page` | `#faf7f0` | `#fff4e0` |
| `--ink` | `#2e3a8c` | `#4a1020` |

1. **TODO 1:** in `:root`, replace `--marigold`, `--indigo` and `--off-white` with the four everyday tokens above.
2. **TODO 2:** make every rule read the tokens, as the comment in `style.css` lists. In particular:
   - `body`: `background-color: var(--page)` and `color: var(--ink)`
   - `.site-header`: `background-color: var(--brand)` and `color: var(--page)`
   - `.price`: `color: var(--brand)`
   - `.buy`: `background-color: var(--accent)` and `color: var(--ink)`
   - `.buy:hover`: `background-color: var(--brand)` and `color: var(--page)`
3. **TODO 3:** add a `.theme-diwali` rule that sets the four tokens to the Diwali values.
4. **TODO 4:** add `.theme-diwali .site-header` with `border-bottom: 4px dashed var(--accent)`.
5. **TODO 5** (`index.html`): write `<body class="theme-diwali">`.

No rule other than `:root` and `.theme-diwali` may type one of these colour codes, and no `--marigold`, `--indigo` or `--off-white` may be left. The greys and the red stay as they are. Values are compared in small letters, so `#FFC93C` and `#ffc93c` both work.

Files to change: `style.css` and `index.html`.
