// A tiny store: one state object, changed only through setState, with subscribers told about every change.

export function createStore(initialState) {
  let state = initialState;
  const listeners = new Set();

  return {
    getState() {
      return state;
    },

    setState(change) {
      const changes = typeof change === "function" ? change(state) : change;
      state = { ...state, ...changes };
      for (const listener of listeners) {
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
