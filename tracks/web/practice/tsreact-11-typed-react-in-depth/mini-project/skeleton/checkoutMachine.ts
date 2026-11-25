import type { Address } from "./address.ts";

/** One shape per step. The upi and done steps can't exist without an address. */
export type CheckoutState =
  | { step: "cart" }
  | { step: "address"; saved: Address | null }
  | { step: "upi"; address: Address }
  | { step: "done"; address: Address; orderId: string };

export type CheckoutAction =
  | { type: "cartConfirmed" }
  | { type: "addressSaved"; address: Address }
  | { type: "wentBack" }
  | { type: "paid"; orderId: string };

export const START: CheckoutState = { step: "cart" };

/** The rules of checkout. An action on the wrong step returns the state unchanged. */
export function checkoutReducer(state: CheckoutState, action: CheckoutAction): CheckoutState {
  switch (action.type) {
    case "cartConfirmed":
      // TODO 3: on the cart step, go to { step: "address", saved: null }.
      return state;
    case "addressSaved":
      // TODO 4: on the address step, go to { step: "upi", address: action.address }.
      return state;
    case "wentBack":
      // TODO 5: address goes back to { step: "cart" }; upi goes back to { step: "address", saved: state.address }.
      return state;
    case "paid":
      // TODO 6: on the upi step, go to { step: "done", address: state.address, orderId: action.orderId }.
      return state;
    default:
      // TODO 7: an action nobody handles: const unknownAction: never = action; then throw
      //         new Error(`Unknown action: ${(unknownAction as { type: string }).type}`).
      return state;
  }
}
