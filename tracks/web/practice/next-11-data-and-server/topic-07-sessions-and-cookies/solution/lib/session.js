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
  const id = randomUUID();
  sessions.set(id, { phone, expiresAt: now + WEEK_SECONDS * 1000 });
  cookieStore.set(SESSION_COOKIE, id, { httpOnly: true, secure: true, sameSite: "lax", path: "/", maxAge: WEEK_SECONDS });
  return id;
}

/** The logged-in customer, { phone }, or null. */
export function currentCustomer(cookieStore, now = Date.now()) {
  const id = cookieStore.get(SESSION_COOKIE)?.value;
  const session = id ? sessions.get(id) : undefined;
  if (!session) return null;
  if (now >= session.expiresAt) {
    sessions.delete(id);
    return null;
  }
  return { phone: session.phone };
}

/** Logs out: forgets the session on the server and removes the cookie. */
export function endSession(cookieStore) {
  const id = cookieStore.get(SESSION_COOKIE)?.value;
  if (id) sessions.delete(id);
  cookieStore.delete(SESSION_COOKIE);
}
