/** Finished: the trimmed text of a setting, or undefined when it is missing or blank. */
export function read(env, name) {
  const value = env[name];
  if (typeof value !== "string" || value.trim() === "") return undefined;
  return value.trim();
}

/**
 * The settings the live site cannot run without, as { name: trimmed value }.
 * requireEnv({ VITE_UPI_ID: " dhaaga@upi " }, ["VITE_UPI_ID"]) is { VITE_UPI_ID: "dhaaga@upi" }.
 */
export function requireEnv(env, names) {
  const values = {};
  const missing = [];
  for (const name of names) {
    const value = read(env, name);
    if (value === undefined) missing.push(name);
    else values[name] = value;
  }
  if (missing.length > 0) {
    throw new Error(`Missing environment values: ${missing.join(", ")}`);
  }
  return values;
}
