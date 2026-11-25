import { OrderForm } from "./OrderForm.jsx";
import { placeOrder } from "./actions.js";

export default function OrderPage() {
  return (
    <main>
      <h1>Place an order</h1>
      <OrderForm action={placeOrder} />
    </main>
  );
}
