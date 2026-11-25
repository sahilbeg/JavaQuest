# Layouts shared by pages

Give every page one shared frame, and give the help centre a frame of its own.

1. **TODO 1 to 3** in `src/app/layout.jsx`, the root layout. Inside `<body>`, in this order:
   - a `<header>` holding two paragraphs: `<p>Dhaaga & Co.</p>` and `<p>Free delivery in Pune above ₹999</p>`,
   - a `<main>` with the page (`{children}`) inside it,
   - a `<footer>` that says `Handmade in Pune`.
2. **TODO 4 and 5** in `src/app/help/layout.jsx`, the layout for every page under `/help`. Inside its `<div>`:
   - an `<h2>` that says `Help centre`, above the page,
   - the page (`{children}`, already there),
   - a `<p>` that says `Still stuck? WhatsApp us on 98765 43210.`, below the page.

The Delivery page in `src/app/help/delivery/page.jsx` is finished. The checks put it inside both layouts to see them nest.

Keep `<html lang="en">` and `<body>` in the root layout only.
