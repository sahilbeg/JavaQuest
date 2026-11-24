import { rupees } from "./money.js";

export function ProductCard({ name, paise, colour = "Off-white" }) {
  return (
    <article className="product-card">
      <h3>{name}</h3>
      <p className="price">{rupees(paise)}</p>
      <p className="colour">Colour: {colour}</p>
    </article>
  );
}

export function Badge({ children }) {
  return <span className="badge">{children}</span>;
}

export function FeaturedProducts() {
  return (
    <section className="featured">
      <h2>
        This week's favourites <Badge>New</Badge>
      </h2>
      <ProductCard name="Golu the elephant" paise={45000} colour="Indigo" />
      <ProductCard name="Marigold coaster set" paise={15000} colour="Marigold" />
      <ProductCard name="Indigo tote bag" paise={89900} colour="Indigo" />
    </section>
  );
}
