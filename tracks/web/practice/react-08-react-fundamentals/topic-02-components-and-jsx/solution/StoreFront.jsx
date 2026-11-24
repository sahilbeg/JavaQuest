// The top of the Dhaaga store, built from small components.

export const TAGLINE = "Handmade crochet from Pune";

export function StoreHeader() {
  return (
    <header className="store-header">
      <h1>Dhaaga & Co.</h1>
      <p>{TAGLINE}</p>
      <img src="golu.png" alt="Golu the crocheted elephant" />
    </header>
  );
}

export function OpeningHours() {
  return (
    <ul className="hours">
      <li>FC Road stall: Sat and Sun, 10:00 to 20:00</li>
      <li>Aundh shop: Tue to Sun, 11:00 to 21:00</li>
    </ul>
  );
}

export function StoreFront() {
  return (
    <main>
      <StoreHeader />
      <OpeningHours />
    </main>
  );
}
