// Sessions, as in topic 7: session id -> { phone }. Expiry is left out to keep this ticket short.
const sessions = new Map();

/** Forgets every session. The checks call this before each test. */
export function resetSessions() {
  sessions.clear();
}

/** Remembers a session. The login action calls this; the checks use it to log customers in. */
export function addSession(id, phone) {
  sessions.set(id, { phone });
}

/** The logged-in customer, { phone }, or null. */
export function currentCustomer(cookieStore) {
  const id = cookieStore.get("dhaaga_session")?.value;
  const session = id ? sessions.get(id) : undefined;
  return session ? { phone: session.phone } : null;
}
