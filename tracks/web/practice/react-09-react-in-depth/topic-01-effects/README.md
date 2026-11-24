# Effects with useEffect

Sana wants the cart count in the browser tab, and a sale banner that really counts down.

Change `src/CartTitle.jsx` and `src/SaleCountdown.jsx`:

1. **TODO 1:** in `CartTitle`, add an effect that sets `document.title`.
   - With 3 items it is `(3) Dhaaga & Co.`.
   - With an empty cart it is just `Dhaaga & Co.`.
   - It runs again only when `count` changes, not on every render.
2. **TODO 2:** in `SaleCountdown`, start a timer after the first render that takes one second off `left` every 1000 ms. It stops at 0, where the banner says `The Diwali sale is over.`
3. **TODO 3:** stop the timer when the banner leaves the page.

`src/main.jsx` shows both in the preview. The checks don't use it.
