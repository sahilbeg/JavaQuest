export const startCheckout = { step: "cart", address: null, orderId: null };

/** The checkout's steps: cart -> address -> upi -> done. An action on the wrong step changes nothing. */
export function checkoutReducer(state, action) {
  switch (action.type) {
    // TODO 3: "cartConfirmed": on the cart step, go to "address".
    // TODO 4: "addressSaved": on the address step, go to "upi" and keep action.address.
    // TODO 5: "wentBack": from "address" go to "cart"; from "upi" go to "address". Keep the address.
    // TODO 6: "paid": on the upi step, go to "done" and keep action.orderId.
    default:
      // TODO 7: throw new Error(`Unknown action: ${action.type}`) instead.
      return state;
  }
}
