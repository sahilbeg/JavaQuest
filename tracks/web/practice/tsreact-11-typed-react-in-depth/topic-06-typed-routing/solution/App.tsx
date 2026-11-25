import { Link } from "./Link.tsx";
import { assertNever } from "./routes.ts";
import type { Route } from "./routes.ts";
import { useRoute } from "./useRoute.ts";

const ITEMS: Record<string, { name: string; price: string }> = {
  "DC-1042": { name: "Golu the elephant", price: "₹450.00" },
  "DC-2001": { name: "Marigold coaster set", price: "₹150.00" },
};

function ShopPage() {
  return (
    <>
      <h1>All items</h1>
      <ul>
        {Object.entries(ITEMS).map(([id, item]) => (
          <li key={id}>
            <Link to={{ page: "item", id }}>{item.name}</Link>
          </li>
        ))}
      </ul>
    </>
  );
}

function ItemPage({ id }: { id: string }) {
  const item = ITEMS[id];
  if (!item) {
    return (
      <>
        <h1>Item not found</h1>
        <p>We have no item {id}.</p>
      </>
    );
  }
  return (
    <>
      <h1>{item.name}</h1>
      <p>{item.price}</p>
    </>
  );
}

function OrderPage({ orderId }: { orderId: string }) {
  return <h1>Order {orderId}</h1>;
}

function NotFoundPage({ path }: { path: string }) {
  return (
    <>
      <h1>Page not found</h1>
      <p>Nothing lives at {path}.</p>
    </>
  );
}

/** The screen for a route. */
function Page({ route }: { route: Route }) {
  switch (route.page) {
    case "shop":
      return <ShopPage />;
    case "item":
      return <ItemPage id={route.id} />;
    case "order":
      return <OrderPage orderId={route.orderId} />;
    case "notFound":
      return <NotFoundPage path={route.path} />;
    default:
      return assertNever(route);
  }
}

export function App() {
  const route = useRoute();
  return (
    <>
      <nav>
        <Link to={{ page: "shop" }}>Shop</Link> <Link to={{ page: "order", orderId: "order-17" }}>My order</Link>
      </nav>
      <main>
        <Page route={route} />
      </main>
    </>
  );
}
