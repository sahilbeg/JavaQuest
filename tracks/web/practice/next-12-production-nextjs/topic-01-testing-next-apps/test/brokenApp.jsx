// Broken copies of the app's parts, one bug each. Your checks must notice every one of them.
import { notFound } from "next/navigation";
import { getItem } from "../src/lib/catalogue.js";
import { addOrder } from "../src/lib/orders.js";
import { formatRupees } from "../src/lib/money.js";

/** Sends the stock as text: { id: "DC-1042", stock: "7" }. */
export async function StockAsText(request) {
  const id = new URL(request.url).searchParams.get("id");
  if (!id) return Response.json({ error: "Missing id" }, { status: 400 });
  const item = await getItem(id);
  if (!item) return Response.json({ error: `No item ${id}` }, { status: 404 });
  return Response.json({ id: item.id, stock: String(item.stock) });
}

/** Answers 200 with stock 0 for an item that doesn't exist. */
export async function StockUnknownIsFine(request) {
  const id = new URL(request.url).searchParams.get("id");
  if (!id) return Response.json({ error: "Missing id" }, { status: 400 });
  const item = await getItem(id);
  if (!item) return Response.json({ id, stock: 0 });
  return Response.json({ id: item.id, stock: item.stock });
}

/** Shows paise as if they were rupees: ₹45000.00. */
export async function PriceInPaise({ params }) {
  const { id } = await params;
  const item = await getItem(id);
  if (!item) notFound();
  return (
    <main>
      <h1>{item.name}</h1>
      <p>₹{item.paise}.00</p>
    </main>
  );
}

/** Reads params.id without awaiting params, as in Next.js 14. In Next.js 16, params is a Promise. */
export async function ParamsNotAwaited({ params }) {
  const item = await getItem(params.id);
  if (!item) notFound();
  return (
    <main>
      <h1>{item.name}</h1>
      <p>{formatRupees(item.paise)}</p>
    </main>
  );
}

/** Shows a normal page saying "Item not found" instead of calling notFound(), so the answer is 200, not 404. */
export async function NoNotFound({ params }) {
  const { id } = await params;
  const item = await getItem(id);
  if (!item) {
    return (
      <main>
        <p>Item not found</p>
      </main>
    );
  }
  return (
    <main>
      <h1>{item.name}</h1>
      <p>{formatRupees(item.paise)}</p>
    </main>
  );
}

/** Never checks the quantity, so 0 Golus become an order. */
export async function AcceptsZero(formData) {
  const item = await getItem(String(formData.get("id")));
  const qty = Number(formData.get("qty"));
  if (!item) return { ok: false, error: "Unknown item" };
  const order = addOrder({ itemId: item.id, qty, totalPaise: item.paise * qty });
  return { ok: true, orderId: order.id, totalPaise: order.totalPaise };
}

/** Charges for one item, whatever the quantity. */
export async function IgnoresQuantity(formData) {
  const item = await getItem(String(formData.get("id")));
  const qty = Number(formData.get("qty"));
  if (!item) return { ok: false, error: "Unknown item" };
  if (!Number.isInteger(qty) || qty < 1 || qty > 5) return { ok: false, error: "Pick a quantity from 1 to 5" };
  const order = addOrder({ itemId: item.id, qty, totalPaise: item.paise });
  return { ok: true, orderId: order.id, totalPaise: order.totalPaise };
}
