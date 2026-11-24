// The top of the Dhaaga store, built from small components.

export const TAGLINE = "Handmade crochet from Pune";

// TODO 1: Return a <header className="store-header"> that holds, in this order:
//   an <h1> saying Dhaaga & Co., a <p> showing TAGLINE (use curly braces),
//   and <img src="golu.png" alt="Golu the crocheted elephant" />.
export function StoreHeader() {
  return <header></header>;
}

// TODO 2: Return a <ul className="hours"> with two <li> items, in this order:
//   FC Road stall: Sat and Sun, 10:00 to 20:00
//   Aundh shop: Tue to Sun, 11:00 to 21:00
export function OpeningHours() {
  return <ul></ul>;
}

// TODO 3: Return a <main> that shows <StoreHeader /> and then <OpeningHours />.
export function StoreFront() {
  return <main></main>;
}
