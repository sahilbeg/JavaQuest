import { randomUUID } from "node:crypto";

export const SESSION_COOKIE = "dhaaga_session";
export const WEEK_SECONDS = 60 * 60 * 24 * 7;

// The server's list of sessions: session id -> { phone, expiresAt }. A real shop would keep it in a database.
const sessions = new Map();

/** Forgets every session. The checks call this before each test. */
export function resetSessions() {
  sessions.clear();
}

/** Logs a customer in: remembers a new session and puts its id in the cookie. Returns the id. */
export function startSession(cookieStore, phone, now = Date.now()) {
  // TODO 1: const id = randomUUID(); remember { phone, expiresAt: now + WEEK_SECONDS * 1000 } for it in sessions,
  // then cookieStore.set(SESSION_COOKIE, id, { httpOnly: true, secure: true, sameSite: "lax", path: "/", maxAge: WEEK_SECONDS }).
  // Return the id. Never put the phone number in the cookie.
  return "";
}

/** The logged-in customer, { phone }, or null. */
export function currentCustomer(cookieStore, now = Date.now()) {
  // TODO 2: read the id with cookieStore.get(SESSION_COOKIE)?.value and look it up in sessions.
  // Return null for no cookie, an unknown id, or a session whose expiresAt has been reached (forget that one).
  return null;
}

/** Logs out: forgets the session on the server and removes the cookie. */
export function endSession(cookieStore) {
  // TODO 3: sessions.delete(the id from the cookie), then cookieStore.delete(SESSION_COOKIE).
}
