// A tiny store: one state object, changed only through setState, with subscribers told about every change.

export function createStore(initialState) {
  let state = initialState;
  // TODO 1: keep the subscribers in a Set

  return {
    getState() {
      return state;
    },

    setState(change) {
      // TODO 2: change is an object of changes, or a function of the state that returns them;
      //         make a NEW state with { ...state, ...changes }
      // TODO 3: then call every subscriber with the new state
    },

    subscribe(listener) {
      // TODO 4: add the listener, and return a function that removes it
      return () => {};
    },
  };
}
