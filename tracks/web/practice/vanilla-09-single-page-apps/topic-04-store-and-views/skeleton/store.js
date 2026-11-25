// The app's one store, like the one from the components module. This file is finished.
// setState takes an update function: it gets the old state and returns a new one.

export function createStore(initial) {
  let state = initial;
  const listeners = new Set();
  return {
    getState() {
      return state;
    },
    setState(update) {
      state = update(state);
      for (const listener of [...listeners]) {
        listener(state);
      }
    },
    subscribe(listener) {
      listeners.add(listener);
      return () => {
        listeners.delete(listener);
      };
    },
  };
}
