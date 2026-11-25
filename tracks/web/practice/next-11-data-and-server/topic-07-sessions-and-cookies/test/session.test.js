import { describe, it, expect, beforeEach } from "vitest";
import { startSession, currentCustomer, endSession, resetSessions, SESSION_COOKIE, WEEK_SECONDS } from "../src/lib/session.js";
import { fakeCookies } from "./cookies.js";

const ASHA = "9876543210";
const WEEK_MS = WEEK_SECONDS * 1000;

beforeEach(() => {
  resetSessions();
});

describe("startSession", () => {
  it("puts a random session id in the cookie, never the phone number", () => {
    const store = fakeCookies();
    const id = startSession(store, ASHA, 0);
    const value = store.get(SESSION_COOKIE)?.value;
    expect(typeof value === "string" && value.length >= 16, "the dhaaga_session cookie should hold a long random id (use randomUUID())").toBe(true);
    expect(value, "startSession should return the same id it put in the cookie").toBe(id);
    expect(value.includes(ASHA), "the phone number must never go into the cookie").toBe(false);
  });

  it("sets the cookie so scripts can't read it and it only travels over HTTPS", () => {
    const store = fakeCookies();
    startSession(store, ASHA, 0);
    const options = store.setCalls.find((call) => call.name === SESSION_COOKIE)?.options ?? {};
    expect(options.httpOnly, "the session cookie needs httpOnly: true").toBe(true);
    expect(options.secure, "the session cookie needs secure: true").toBe(true);
    expect(options.sameSite, 'the session cookie needs sameSite: "lax"').toBe("lax");
    expect(options.path, 'the session cookie needs path: "/"').toBe("/");
    expect(options.maxAge, "the session cookie should last one week: maxAge: WEEK_SECONDS").toBe(WEEK_SECONDS);
  });

  it("gives two customers different session ids", () => {
    const first = startSession(fakeCookies(), ASHA, 0);
    const second = startSession(fakeCookies(), "9876543211", 0);
    expect(first === second, "every login should get its own random id").toBe(false);
  });
});

describe("currentCustomer", () => {
  it("finds the customer from their session cookie", () => {
    const store = fakeCookies();
    startSession(store, ASHA, 0);
    expect(currentCustomer(store, 1000), "the cookie set at login should find Asha").toEqual({ phone: ASHA });
  });

  it("gives null when there is no cookie", () => {
    expect(currentCustomer(fakeCookies(), 0), "a visitor with no cookie is nobody (read it with ?.value)").toBeNull();
  });

  it("gives null for a cookie someone made up", () => {
    startSession(fakeCookies(), ASHA, 0);
    const forged = fakeCookies({ [SESSION_COOKIE]: ASHA });
    expect(currentCustomer(forged, 1000), "typing a phone number into the cookie must not log you in").toBeNull();
  });

  it("keeps the session for a week and no longer", () => {
    const store = fakeCookies();
    startSession(store, ASHA, 0);
    expect(currentCustomer(store, WEEK_MS - 1), "one millisecond before a week is up, Asha is still logged in").toEqual({ phone: ASHA });
    expect(currentCustomer(store, WEEK_MS), "once now reaches expiresAt, the session is over").toBeNull();
  });
});

describe("endSession", () => {
  it("removes the cookie", () => {
    const store = fakeCookies();
    startSession(store, ASHA, 0);
    endSession(store);
    expect(store.get(SESSION_COOKIE), "after logging out, the dhaaga_session cookie should be gone").toBeUndefined();
  });

  it("forgets the session on the server, so a copied cookie stops working", () => {
    const store = fakeCookies();
    const id = startSession(store, ASHA, 0);
    const copy = fakeCookies({ [SESSION_COOKIE]: id });
    expect(currentCustomer(copy, 1000), "before logging out, the copied cookie works").toEqual({ phone: ASHA });
    endSession(store);
    expect(currentCustomer(copy, 1000), "after logging out, the copied cookie must not work (sessions.delete)").toBeNull();
  });
});
