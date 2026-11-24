import { vi } from "vitest";

function reply(status, body) {
  return { ok: status >= 200 && status < 300, status, json: async () => body };
}

/**
 * A pretend Dhaaga API, installed as the global fetch. It answers:
 *   GET  /api/delivery?pin=411001  -> 200 { deliverable: true, days: 2 } for Pune PINs (they start with 411)
 *                                     200 { deliverable: false } for other 6-digit PINs, 400 otherwise
 *   POST /api/orders (JSON body)   -> 201 { id: "order-17" }, or 400 { error: "PIN must be 6 digits" }
 */
export function pretendApi() {
  const fetchMock = vi.fn(async (url, options = {}) => {
    const address = new URL(String(url), "https://dhaaga.in");
    const method = options.method ?? "GET";
    if (address.pathname === "/api/delivery" && method === "GET") {
      const pin = address.searchParams.get("pin") ?? "";
      if (!/^\d{6}$/.test(pin)) return reply(400, { error: "PIN must be 6 digits" });
      return reply(200, pin.startsWith("411") ? { deliverable: true, days: 2 } : { deliverable: false });
    }
    if (address.pathname === "/api/orders" && method === "POST") {
      let order;
      try {
        order = JSON.parse(options.body);
      } catch {
        return reply(400, { error: "The body must be JSON" });
      }
      if (!/^\d{6}$/.test(String(order?.pin))) return reply(400, { error: "PIN must be 6 digits" });
      return reply(201, { id: "order-17" });
    }
    return reply(404, { error: "Not found" });
  });
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}
