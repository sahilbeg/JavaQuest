/** A customer's website, if it is a full http or https address. Anything else gives null. */
export function safeUrl(url) {
  try {
    const { protocol } = new URL(url);
    return protocol === "https:" || protocol === "http:" ? url : null;
  } catch {
    return null;
  }
}
