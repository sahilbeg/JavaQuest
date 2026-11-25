import { describe, it, expect } from "vitest";
import { proxy, config } from "../src/proxy.js";
import { fakeRequest } from "./fakes.js";

describe("proxy: old catalogue links", () => {
  it("sends /catalogue to /items for good", () => {
    const res = proxy(fakeRequest("/catalogue"));
    expect(res?.status, "a page that moved for good answers 308").toBe(308);
    expect(res?.headers.get("location"), "/catalogue should go to /items").toBe("http://localhost/items");
  });

  it("keeps the rest of the path", () => {
    const res = proxy(fakeRequest("/catalogue/DC-1042"));
    expect(res?.status, "an old item link answers 308").toBe(308);
    expect(res?.headers.get("location"), "/catalogue/DC-1042 should go to /items/DC-1042").toBe("http://localhost/items/DC-1042");
  });
});

describe("proxy: account pages", () => {
  it("sends a visitor with no session cookie to the login page, remembering where they were going", () => {
    const res = proxy(fakeRequest("/account/orders"));
    expect(res?.status, "a logged-out visitor should get a temporary redirect, 307").toBe(307);
    expect(res?.headers.get("location"), "the redirect should go to /login?next=/account/orders").toBe("http://localhost/login?next=/account/orders");
  });

  it("guards /account itself too", () => {
    const res = proxy(fakeRequest("/account"));
    expect(res?.headers.get("location"), "/account with no cookie should go to /login?next=/account").toBe("http://localhost/login?next=/account");
  });

  it("lets a visitor with a session cookie through", () => {
    const res = proxy(fakeRequest("/account/orders", { dhaaga_session: "s-asha" }));
    expect(res, "with a dhaaga_session cookie, the proxy should return nothing, so the page runs").toBeUndefined();
  });
});

describe("proxy config", () => {
  it("runs only for the account and catalogue paths", () => {
    expect([...(config?.matcher ?? [])].sort(), 'config.matcher should be ["/account/:path*", "/catalogue/:path*"]').toEqual([
      "/account/:path*",
      "/catalogue/:path*",
    ]);
  });
});
