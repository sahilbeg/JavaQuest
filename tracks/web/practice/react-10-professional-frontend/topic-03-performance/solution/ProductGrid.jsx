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
          <img
            src={photoUrl(product.id, 400)}
            srcSet={photoSrcSet(product.id)}
            sizes="(min-width: 600px) 400px, 100vw"
            alt={product.name}
            width={400}
            height={400}
            loading={index < ABOVE_THE_FOLD ? "eager" : "lazy"}
            decoding="async"
          />
          <h3>{product.name}</h3>
          <p>{formatRupees(product.paise)}</p>
        </li>
      ))}
    </ul>
  );
}
