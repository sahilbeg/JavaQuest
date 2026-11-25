// Round 1, JavaScript: "Write your own Promise.all."

/** Resolves with every value's result, in the order given, or rejects with the first rejection. */
export function promiseAll(values) {
  return new Promise((resolve, reject) => {
    const results = new Array(values.length);
    let left = values.length;
    if (left === 0) {
      resolve(results);
      return;
    }
    values.forEach((value, index) => {
      Promise.resolve(value).then((result) => {
        results[index] = result;
        left -= 1;
        if (left === 0) {
          resolve(results);
        }
      }, reject);
    });
  });
}
