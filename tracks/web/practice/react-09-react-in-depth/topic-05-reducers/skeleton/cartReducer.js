export const emptyCart = { lines: [] };

/** Every way the cart can change. Returns the next state and never changes the one it was given. */
export function cartReducer(state, action) {
  switch (action.type) {
    // TODO 1: "added": add action.item with qty 1, or raise the qty of the line with the same code by 1.
    // TODO 2: "removed": drop the line whose code is action.code.
    // TODO 3: "quantityChanged": set that line's qty to action.qty; a qty of 0 or less removes the line.
    // TODO 4: "cleared": return an empty cart.
    default:
      // TODO 5: throw new Error(`Unknown action: ${action.type}`) instead of returning the state.
      return state;
  }
}
