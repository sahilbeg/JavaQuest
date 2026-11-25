// TODO 2: error pages run in the browser. Make the very first line of the file the "use client" directive.

/** Shown when the shop page throws an error. */
export default function ShopError({ error, retry }) {
  // TODO 3: a <div role="alert"> with the kind heading, the sentence and a Try again button that calls retry().
  // Never show the error itself to customers.
  return <p>{String(error)}</p>;
}
