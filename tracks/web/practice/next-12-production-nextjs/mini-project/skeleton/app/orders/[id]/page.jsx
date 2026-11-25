import { notFound } from "next/navigation";
import { getOrder } from "../../../lib/store.js";

// The thank-you page placeOrder redirects to. Already done. It shows no personal details, because anyone can type an order id.
export const metadata = { title: "Thank you", robots: { index: false } };

export default async function OrderPage({ params }) {
  const { id } = await params;
  const order = await getOrder(id);
  if (!order) {
    notFound();
  }
  return (
    <section aria-labelledby="thanks-heading">
      <h1 id="thanks-heading">Thank you!</h1>
      <p>Your order {order.id} is placed. We'll send you a WhatsApp message when it ships.</p>
    </section>
  );
}
