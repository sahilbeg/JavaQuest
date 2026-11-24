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
  for (const name of names) {
    // TODO 2: use read(env, name), so values are trimmed and blank ones count as missing.
    values[name] = env[name];
  }
  // TODO 3: if any names are missing, throw one Error that lists every missing name, in order, joined with ", ".
  return values;
}
