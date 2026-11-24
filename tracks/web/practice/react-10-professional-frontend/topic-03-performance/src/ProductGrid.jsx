import { formatRupees } from "./money.js";
import { photoSrcSet, photoUrl } from "./photos.js";

/** How many photos a phone shows before the customer scrolls. */
export const ABOVE_THE_FOLD = 4;

/** The catalogue grid: one card per product, each with its photo. */
export function ProductGrid({ products }) {
  return (
    <ul className="grid">
      {products.map((product, index) => (
        <li key={product.id} className="card">
          {/* TODO 2: use the 400-pixel photo as src, and add srcSet={photoSrcSet(product.id)} and sizes="(min-width: 600px) 400px, 100vw". */}
          {/* TODO 3: add alt={product.name}, width={400} and height={400}, so the browser keeps the space before the photo arrives. */}
          {/* TODO 4: the first ABOVE_THE_FOLD photos get loading="eager"; every other photo gets loading="lazy". Add decoding="async" too. */}
          <img src={photoUrl(product.id, 1600)} />
          <h3>{product.name}</h3>
          <p>{formatRupees(product.paise)}</p>
        </li>
      ))}
    </ul>
  );
}
