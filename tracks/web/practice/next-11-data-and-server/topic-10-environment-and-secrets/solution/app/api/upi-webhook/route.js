/** POST /api/upi-webhook: the UPI gateway tells us a customer has paid. */
export async function POST(request) {
  const secret = process.env.UPI_WEBHOOK_SECRET;
  if (!secret) return Response.json({ error: "Webhook is not set up." }, { status: 500 });
  if (request.headers.get("x-dhaaga-signature") !== secret) {
    return Response.json({ error: "Bad signature." }, { status: 401 });
  }
  return Response.json({ ok: true });
}
