import { describe, it, expect, beforeEach, vi } from "vitest";

vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
  redirect: vi.fn((to) => {
    throw new Error("NEXT_REDIRECT " + to);
  }),
  useRouter: () => ({ push: vi.fn() }),
  usePathname: () => "/",
}));

import { requireCustomer, getOwnOrder } from "../src/lib/guard.js";
import { addSession, resetSessions } from "../src/lib/session.js";
import { fakeCookies } from "./fakes.js";

const ASHA = fakeCookies({ dhaaga_session: "s-asha" });
const RAVI = fakeCookies({ dhaaga_session: "s-ravi" });
const NOBODY = fakeCookies();

beforeEach(() => {
  resetSessions();
  addSession("s-asha", "9876543210");
  addSession("s-ravi", "9876543211");
});

describe("requireCustomer", () => {
  it("gives the logged-in customer", () => {
    expect(requireCustomer(ASHA, "/account"), "Asha's session should give her phone").toEqual({ phone: "9876543210" });
  });

  it("sends a logged-out visitor to the login page", () => {
    expect(() => requireCustomer(NOBODY, "/account/orders"), "with no customer, call redirect(\"/login?next=/account/orders\")").toThrow(
      "NEXT_REDIRECT /login?next=/account/orders",
    );
  });

  it("doesn't trust a made-up cookie", () => {
    expect(() => requireCustomer(fakeCookies({ dhaaga_session: "9876543210" }), "/account"), "a cookie with no session behind it is nobody").toThrow(
      "NEXT_REDIRECT /login?next=/account",
    );
  });
});

describe("getOwnOrder", () => {
  it("gives a customer their own order", () => {
    expect(getOwnOrder(ASHA, "order-17"), "order-17 is Asha's").toEqual({ id: "order-17", phone: "9876543210", status: "NEW" });
  });

  it("shows not found for someone else's order", () => {
    expect(() => getOwnOrder(RAVI, "order-17"), "Ravi must not see Asha's order: call notFound()").toThrow("NEXT_NOT_FOUND");
  });

  it("shows not found for an order that doesn't exist", () => {
    expect(() => getOwnOrder(ASHA, "order-99"), "a missing order should call notFound()").toThrow("NEXT_NOT_FOUND");
  });

  it("sends a logged-out visitor to the login page, back to this order", () => {
    expect(() => getOwnOrder(NOBODY, "order-17"), "log in first, then come back to /account/orders/order-17").toThrow(
      "NEXT_REDIRECT /login?next=/account/orders/order-17",
    );
  });
});
