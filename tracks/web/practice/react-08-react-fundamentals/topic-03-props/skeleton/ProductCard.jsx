import { rupees } from "./money.js";

// TODO 1: Unpack name, paise and colour from the props, with colour defaulting to "Off-white".
// TODO 2: Show name in the <h3>, rupees(paise) in p.price, and "Colour: " plus the colour in p.colour.
export function ProductCard(props) {
  return (
    <article className="product-card">
      <h3></h3>
      <p className="price"></p>
      <p className="colour"></p>
    </article>
  );
}

// TODO 3: Show the children prop inside the span.
export function Badge(props) {
  return <span className="badge"></span>;
}

// TODO 4: After the <h2>, show three ProductCards, in this order:
//   Golu the elephant, 45000 paise, Indigo
//   Marigold coaster set, 15000 paise, Marigold
//   Indigo tote bag, 89900 paise, Indigo
export function FeaturedProducts() {
  return (
    <section className="featured">
      <h2>
        This week's favourites <Badge>New</Badge>
      </h2>
    </section>
  );
}
