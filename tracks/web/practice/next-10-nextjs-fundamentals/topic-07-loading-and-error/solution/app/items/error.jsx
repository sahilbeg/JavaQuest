"use client";

/** Shown when the shop page throws an error. */
export default function ShopError({ error, retry }) {
  return (
    <div role="alert">
      <h2>We couldn't load the shop.</h2>
      <p>Please try again in a moment.</p>
      <button type="button" onClick={() => retry()}>
        Try again
      </button>
    </div>
  );
}
