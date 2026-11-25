import { describe, it, expect } from "vitest";
import { promiseAll } from "../src/promiseAll.js";

/** A promise the test resolves or rejects by hand, so it decides which answer arrives first. */
function deferred() {
  let resolve;
  let reject;
  const promise = new Promise((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

/** Keeps track of how a promise ended: "pending", "fulfilled" or "rejected", and with what. */
function watch(promise) {
  const seen = { status: "pending", value: undefined };
  promise.then(
    (value) => {
      seen.status = "fulfilled";
      seen.value = value;
    },
    (error) => {
      seen.status = "rejected";
      seen.value = error;
    },
  );
  return seen;
}

/** Lets every waiting promise callback run. */
async function settle() {
  for (let i = 0; i < 20; i += 1) {
    await Promise.resolve();
  }
}

describe("Bronze: promiseAll", () => {
  it("resolves an empty list straight away, with []", async () => {
    const seen = watch(promiseAll([]));
    await settle();
    expect(seen.status, "promiseAll([]) should resolve at once; with nothing to wait for, it must not hang").toBe("fulfilled");
    expect(seen.value, "promiseAll([]) should give []").toEqual([]);
  });

  it("keeps the order of the input, not the order the answers arrive", async () => {
    const golu = deferred();
    const tote = deferred();
    const keychain = deferred();
    const seen = watch(promiseAll([golu.promise, tote.promise, keychain.promise]));
    keychain.resolve(12);
    golu.resolve(7);
    tote.resolve(0);
    await settle();
    expect(seen.status, "all three answered, so promiseAll should have resolved").toBe("fulfilled");
    expect(seen.value, "results must follow the input order (Golu 7, tote 0, keychain 12), not the arrival order").toEqual([7, 0, 12]);
  });

  it("waits until every promise has answered", async () => {
    const golu = deferred();
    const tote = deferred();
    const keychain = deferred();
    const seen = watch(promiseAll([golu.promise, tote.promise, keychain.promise]));
    golu.resolve(7);
    tote.resolve(0);
    await settle();
    expect(seen.status, "the keychain hasn't answered yet, so promiseAll must still be waiting").toBe("pending");
  });

  it("counts an answer of 0 like any other", async () => {
    const seen = watch(promiseAll([Promise.resolve(0), Promise.resolve(0)]));
    await settle();
    expect(seen.value, "two answers of 0 stock should give [0, 0]").toEqual([0, 0]);
  });

  it("accepts plain values as well as promises", async () => {
    const seen = watch(promiseAll([45000, Promise.resolve(15000)]));
    await settle();
    expect(seen.status, "a plain value like 45000 counts as already answered; wrap each value with Promise.resolve").toBe("fulfilled");
    expect(seen.value, "the results should be [45000, 15000]").toEqual([45000, 15000]);
  });

  it("rejects with the first error, without waiting for the rest", async () => {
    const golu = deferred();
    const tote = deferred();
    const seen = watch(promiseAll([golu.promise, tote.promise]));
    tote.reject(new Error("DC-1301 is out of stock"));
    await settle();
    expect(seen.status, "one rejection should reject promiseAll at once, even though Golu never answered").toBe("rejected");
    expect(seen.value?.message, "promiseAll should reject with the tote bag's own error").toBe("DC-1301 is out of stock");
  });
});
