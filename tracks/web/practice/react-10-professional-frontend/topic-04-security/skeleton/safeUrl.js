/** A customer's website, if it is a full http or https address. Anything else gives null. */
export function safeUrl(url) {
  // TODO 1: parse url with new URL(url) inside try/catch. Return url only when its protocol is "https:" or "http:". Otherwise, or when parsing throws, return null.
  return url;
}
