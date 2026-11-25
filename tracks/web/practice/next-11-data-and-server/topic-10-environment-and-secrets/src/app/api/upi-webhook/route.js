/** POST /api/upi-webhook: the UPI gateway tells us a customer has paid. */
export async function POST(request) {
  // TODO 4: read process.env.UPI_WEBHOOK_SECRET here, inside the handler.
  // Missing or empty: answer 500 { error: "Webhook is not set up." }.
  // TODO 5: request.headers.get("x-dhaaga-signature") must be exactly the secret.
  // If not: 401 { error: "Bad signature." }. Otherwise: { ok: true }.
  return Response.json({ error: "Not built yet." }, { status: 501 });
}
