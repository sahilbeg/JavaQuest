// Round 1, JavaScript: "Write your own Promise.all."
// The candidate's answer. It worked for their one test: three promises that answered in order.

/** Resolves with every value's result, in the order given, or rejects with the first rejection. */
export function promiseAll(values) {
  // TODO 1: fix the four bugs: an empty list must resolve with [] at once; results must keep the input's order
  //         (store each at its index, and count how many are left); plain values like 45000 must work
  //         (wrap each with Promise.resolve); and the first rejection must reject the whole promise
  return new Promise((resolve, reject) => {
    const results = [];
    values.forEach((value) => {
      value.then((result) => {
        results.push(result);
        if (results.length === values.length) {
          resolve(results);
        }
      });
    });
  });
}
