/** A small store: getState(), dispatch(action) runs the reducer and tells every subscriber, subscribe(fn) returns an unsubscribe. */
export function createStore(reducer, state) {
  const listeners = new Set();
  return {
    getState: () => state,
    dispatch(action) {
      state = reducer(state, action);
      for (const listener of listeners) {
        listener(state);
      }
    },
    subscribe(listener) {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
  };
}
