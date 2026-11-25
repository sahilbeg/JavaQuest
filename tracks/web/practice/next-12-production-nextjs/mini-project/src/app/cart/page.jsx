import { cookies } from "next/headers";
import { readCart } from "../../lib/cart.js";
import { CartView } from "../../components/CartView.jsx";

// The cart page reads the cart cookie on the server. Already done: the checks test readCart and CartView instead,
// because next/headers only works inside a running Next.js server.
export const metadata = { title: "Your cart", robots: { index: false } };

export default async function CartPage() {
  const lines = await readCart(await cookies());
  return <CartView lines={lines} />;
}
