# Fast pages: images, lazy loading and Web Vitals

The Diwali catalogue downloads every photo at full size, all at once, and the grid jumps as each one arrives. Fix the photos. Change `src/photos.js` and `src/ProductGrid.jsx`.

Every product photo is saved on the server in two widths, 400 and 800 pixels, at `/img/<id>-<width>.webp`. `photoUrl(id, width)` already builds one address.

1. **TODO 1:** `photoSrcSet(id)` returns both sizes for `srcset`, each followed by its width and `w`, joined with a comma and a space:
   `"/img/DC-1042-400.webp 400w, /img/DC-1042-800.webp 800w"`.
2. **TODO 2:** each `<img>` uses the 400-pixel file as `src`, and adds `srcSet={photoSrcSet(product.id)}` and `sizes="(min-width: 600px) 400px, 100vw"`.
3. **TODO 3:** each `<img>` has `alt` set to the product's name, `width={400}` and `height={400}`.
4. **TODO 4:** the first `ABOVE_THE_FOLD` (4) photos have `loading="eager"`. Every photo after them has `loading="lazy"`. Add `decoding="async"` to all of them.

The checks read the attributes of every `<img>` in the grid. The preview has no photo files, so it shows alt text in boxes. With `width` and `height` set, those boxes keep the right size.
