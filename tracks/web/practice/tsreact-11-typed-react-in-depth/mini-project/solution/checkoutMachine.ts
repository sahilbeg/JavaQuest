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
      if (state.step !== "cart") return state;
      return { step: "address", saved: null };
    case "addressSaved":
      if (state.step !== "address") return state;
      return { step: "upi", address: action.address };
    case "wentBack":
      if (state.step === "address") return { step: "cart" };
      if (state.step === "upi") return { step: "address", saved: state.address };
      return state;
    case "paid":
      if (state.step !== "upi") return state;
      return { step: "done", address: state.address, orderId: action.orderId };
    default: {
      const unknownAction: never = action;
      throw new Error(`Unknown action: ${(unknownAction as { type: string }).type}`);
    }
  }
}
