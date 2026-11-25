// Settings from the environment. Every function takes env (in the app, process.env) as an argument.
// In the app, this file would start with import "server-only";

const REQUIRED = ["UPI_GATEWAY_KEY", "ORDERS_DB_URL"];

/** The server's secret settings: { upiKey, dbUrl }. Throws when any required one is missing. */
export function readServerConfig(env) {
  // TODO 1: collect the names in REQUIRED whose value is missing or empty.
  // If there are any, throw new Error(`Missing environment variables: ${missing.join(", ")}.`).
  // Otherwise return { upiKey: env.UPI_GATEWAY_KEY, dbUrl: env.ORDERS_DB_URL }.
  return { upiKey: "", dbUrl: "" };
}

/** The settings that may go to the browser: { siteUrl, upiId }. */
export function publicConfig(env) {
  // TODO 2: read only NEXT_PUBLIC_SITE_URL and NEXT_PUBLIC_UPI_ID,
  // with the defaults "https://dhaaga.in" and "dhaaga@upi" when they're missing.
  return { siteUrl: "", upiId: "" };
}

/** Names that would put a secret in the browser, sorted A to Z. */
export function leakedSecrets(env) {
  // TODO 3: the names that start with NEXT_PUBLIC_ and contain KEY, SECRET, TOKEN or PASSWORD, sorted.
  return [];
}
