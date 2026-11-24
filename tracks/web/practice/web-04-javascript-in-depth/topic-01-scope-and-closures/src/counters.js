// The order counter and the stock counter, each locked inside a closure.

export function makeOrderNumbers(start) {
  // TODO 1: make a variable next = start here, then return a function that returns "order-" + next and adds 1 to next
  return function () {
    return "";
  };
}

export function makeStockCounter(initial) {
  // TODO 2: keep the count in a variable here, and make sell, restock and left use it
  return {
    sell: function () {
      return false;
    },
    restock: function (qty) {},
    left: function () {
      return 0;
    },
  };
}

export function makeNumberedLabels(names) {
  const labels = [];
  // TODO 3: every label function sees the same i. Change one word in the loop so each turn gets its own i.
  for (var i = 0; i < names.length; i++) {
    labels.push(function () {
      return i + 1 + ". " + names[i];
    });
  }
  return labels;
}
