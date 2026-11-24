export const startCheckout = { step: "cart", address: null, orderId: null };

/** The checkout's steps: cart -> address -> upi -> done. An action on the wrong step changes nothing. */
export function checkoutReducer(state, action) {
  switch (action.type) {
    case "cartConfirmed":
      if (state.step !== "cart") return state;
      return { ...state, step: "address" };
    case "addressSaved":
      if (state.step !== "address") return state;
      return { ...state, step: "upi", address: action.address };
    case "wentBack":
      if (state.step === "address") return { ...state, step: "cart" };
      if (state.step === "upi") return { ...state, step: "address" };
      return state;
    case "paid":
      if (state.step !== "upi") return state;
      return { ...state, step: "done", orderId: action.orderId };
    default:
      throw new Error(`Unknown action: ${action.type}`);
  }
}
