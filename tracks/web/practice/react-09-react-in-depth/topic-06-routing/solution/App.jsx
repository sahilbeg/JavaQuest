import { Link } from "./Link.jsx";
import { matchRoute } from "./router.js";
import { useHashRoute } from "./useHashRoute.js";
import { ITEMS_BY_ID } from "./items.js";
import { rupees } from "./money.js";

export function App() {
  const route = matchRoute(useHashRoute());
  return (
    <>
      <nav>
        <Link to="/">Home</Link> <Link to="/cart">Cart</Link> <Link to="/item/DC-1042">Golu the elephant</Link>
      </nav>
      <main>
        <Page route={route} />
      </main>
    </>
  );
}

function Page({ route }) {
  switch (route.page) {
    case "home":
      return <HomePage />;
    case "cart":
      return <CartPage />;
    case "item":
      return <ItemPage id={route.id} />;
    default:
      return <NotFound />;
  }
}

function HomePage() {
  return (
    <>
      <h1>Handmade in Pune</h1>
      <p>Coasters, keychains, totes and little elephants, crocheted by hand.</p>
    </>
  );
}

function CartPage() {
  return (
    <>
      <h1>Your cart</h1>
      <p>Your cart is empty.</p>
    </>
  );
}

function ItemPage({ id }) {
  const item = ITEMS_BY_ID[id];
  if (!item) return <h1>We can't find {id}</h1>;
  return (
    <>
      <h1>{item.name}</h1>
      <p>{rupees(item.paise)}</p>
    </>
  );
}

function NotFound() {
  return (
    <>
      <h1>Page not found</h1>
      <Link to="/">Back to the shop</Link>
    </>
  );
}
