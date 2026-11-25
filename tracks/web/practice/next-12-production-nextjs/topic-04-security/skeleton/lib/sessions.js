// Logged-in customers: each session token, kept in the "session" cookie, belongs to one customer.
const SESSIONS = { "s-7f3a9c": "cust-asha", "s-91bc04": "cust-ravi" };

/** The session token from a Cookie header like "theme=dark; session=s-7f3a9c", or null. */
export function sessionFromCookie(header) {
  if (!header) {
    return null;
  }
  for (const part of header.split(";")) {
    const [name, ...rest] = part.trim().split("=");
    if (name === "session") {
      return rest.join("=");
    }
  }
  return null;
}

/** The customer id for a session token, or null when the token is missing or unknown. */
export function customerFor(token) {
  if (!token) {
    return null;
  }
  return SESSIONS[token] ?? null;
}
