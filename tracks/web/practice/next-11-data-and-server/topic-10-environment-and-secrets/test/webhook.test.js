import { describe, it, expect, afterEach, vi } from "vitest";
import { POST } from "../src/app/api/upi-webhook/route.js";

afterEach(() => {
  vi.unstubAllEnvs();
});

/** A payment message from the gateway, with this signature header (or none). */
function paid(signature) {
  const headers = { "content-type": "application/json" };
  if (signature !== undefined) headers["x-dhaaga-signature"] = signature;
  return new Request("http://localhost/api/upi-webhook", {
    method: "POST",
    headers,
    body: JSON.stringify({ orderId: "order-17", status: "PAID" }),
  });
}

describe("POST /api/upi-webhook", () => {
  it("accepts a message with the right secret", async () => {
    vi.stubEnv("UPI_WEBHOOK_SECRET", "test-secret-123");
    const res = await POST(paid("test-secret-123"));
    expect(res.status, "the right signature should answer 200 (read process.env inside POST)").toBe(200);
    expect(await res.json(), "the answer should be { ok: true }").toEqual({ ok: true });
  });

  it("refuses a message with the wrong secret", async () => {
    vi.stubEnv("UPI_WEBHOOK_SECRET", "test-secret-123");
    const res = await POST(paid("guess"));
    expect(res.status, "a wrong signature should answer 401").toBe(401);
    expect(await res.json(), "the answer should say Bad signature.").toEqual({ error: "Bad signature." });
  });

  it("refuses a message with no signature at all", async () => {
    vi.stubEnv("UPI_WEBHOOK_SECRET", "test-secret-123");
    const res = await POST(paid(undefined));
    expect(res.status, "a message without x-dhaaga-signature should answer 401").toBe(401);
  });

  it("refuses everything when the secret isn't set up", async () => {
    vi.stubEnv("UPI_WEBHOOK_SECRET", "");
    const res = await POST(paid(undefined));
    expect(res.status, "with no secret set up, the webhook must refuse with 500, never let it through").toBe(500);
    expect(await res.json(), "the answer should say Webhook is not set up.").toEqual({ error: "Webhook is not set up." });
  });

  it("picks up a changed secret without a restart", async () => {
    vi.stubEnv("UPI_WEBHOOK_SECRET", "old-secret");
    expect((await POST(paid("old-secret"))).status, "the first secret works").toBe(200);
    vi.stubEnv("UPI_WEBHOOK_SECRET", "new-secret");
    expect((await POST(paid("old-secret"))).status, "after the secret changes, the old one must stop working").toBe(401);
    expect((await POST(paid("new-secret"))).status, "and the new one should work").toBe(200);
  });
});
