// The order counter and the stock counter, each locked inside a closure.

export function makeOrderNumbers(start) {
  let next = start;
  return function () {
    const id = "order-" + next;
    next = next + 1;
    return id;
  };
}

export function makeStockCounter(initial) {
  let count = initial;
  return {
    sell: function () {
      if (count === 0) {
        return false;
      }
      count = count - 1;
      return true;
    },
    restock: function (qty) {
      count = count + qty;
    },
    left: function () {
      return count;
    },
  };
}

export function makeNumberedLabels(names) {
  const labels = [];
  for (let i = 0; i < names.length; i++) {
    labels.push(function () {
      return i + 1 + ". " + names[i];
    });
  }
  return labels;
}
