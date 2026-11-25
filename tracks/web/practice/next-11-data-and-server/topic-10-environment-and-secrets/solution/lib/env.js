// Settings from the environment. Every function takes env (in the app, process.env) as an argument.
// In the app, this file would start with import "server-only";

const REQUIRED = ["UPI_GATEWAY_KEY", "ORDERS_DB_URL"];

/** The server's secret settings: { upiKey, dbUrl }. Throws when any required one is missing. */
export function readServerConfig(env) {
  const missing = REQUIRED.filter((name) => !env[name]);
  if (missing.length > 0) throw new Error(`Missing environment variables: ${missing.join(", ")}.`);
  return { upiKey: env.UPI_GATEWAY_KEY, dbUrl: env.ORDERS_DB_URL };
}

/** The settings that may go to the browser: { siteUrl, upiId }. */
export function publicConfig(env) {
  return {
    siteUrl: env.NEXT_PUBLIC_SITE_URL || "https://dhaaga.in",
    upiId: env.NEXT_PUBLIC_UPI_ID || "dhaaga@upi",
  };
}

/** Names that would put a secret in the browser, sorted A to Z. */
export function leakedSecrets(env) {
  return Object.keys(env)
    .filter((name) => name.startsWith("NEXT_PUBLIC_"))
    .filter((name) => /KEY|SECRET|TOKEN|PASSWORD/.test(name))
    .sort();
}
