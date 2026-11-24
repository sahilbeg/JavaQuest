export const emptyCart = { lines: [] };

/** Every way the cart can change. Returns the next state and never changes the one it was given. */
export function cartReducer(state, action) {
  switch (action.type) {
    case "added": {
      const found = state.lines.some((line) => line.code === action.item.code);
      if (found) {
        return {
          ...state,
          lines: state.lines.map((line) => (line.code === action.item.code ? { ...line, qty: line.qty + 1 } : line)),
        };
      }
      return { ...state, lines: [...state.lines, { ...action.item, qty: 1 }] };
    }
    case "removed":
      return { ...state, lines: state.lines.filter((line) => line.code !== action.code) };
    case "quantityChanged":
      if (action.qty <= 0) {
        return { ...state, lines: state.lines.filter((line) => line.code !== action.code) };
      }
      return {
        ...state,
        lines: state.lines.map((line) => (line.code === action.code ? { ...line, qty: action.qty } : line)),
      };
    case "cleared":
      return emptyCart;
    default:
      throw new Error(`Unknown action: ${action.type}`);
  }
}
