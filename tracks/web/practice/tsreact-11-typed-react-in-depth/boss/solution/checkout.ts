export type Step = "cart" | "address" | "gift" | "upi" | "done";

export interface CheckoutState {
  step: Step;
  giftWrap: boolean;
  stitchedName: string;
}

export type CheckoutAction =
  | { type: "next" }
  | { type: "back" }
  | { type: "giftChosen"; giftWrap: boolean; stitchedName: string };

export const START: CheckoutState = { step: "cart", giftWrap: false, stitchedName: "" };

/** Where Continue goes from each step. A Record must name every step, so none can be forgotten. */
const NEXT: Record<Step, Step | null> = { cart: "address", address: "gift", gift: "upi", upi: "done", done: null };

/** Where Back goes from each step. */
const BACK: Record<Step, Step | null> = { cart: null, address: "cart", gift: "address", upi: "gift", done: null };

export function checkoutReducer(state: CheckoutState, action: CheckoutAction): CheckoutState {
  switch (action.type) {
    case "next": {
      const to = NEXT[state.step];
      return to === null ? state : { ...state, step: to };
    }
    case "back": {
      const to = BACK[state.step];
      return to === null ? state : { ...state, step: to };
    }
    case "giftChosen":
      return { ...state, giftWrap: action.giftWrap, stitchedName: action.stitchedName };
    default: {
      const unknownAction: never = action;
      throw new Error(`Unknown action: ${(unknownAction as { type: string }).type}`);
    }
  }
}

/** Gift wrap is ₹30 and a stitched name ₹50, in paise. */
export function extrasPaise(state: CheckoutState): number {
  return (state.giftWrap ? 3000 : 0) + (state.stitchedName.trim() !== "" ? 5000 : 0);
}

/** The heading for each step. */
export function stepTitle(step: Step): string {
  switch (step) {
    case "cart":
      return "Your cart";
    case "address":
      return "Delivery address";
    case "gift":
      return "Make it a gift";
    case "upi":
      return "Pay by UPI";
    case "done":
      return "Order placed";
    default: {
      const unknownStep: never = step;
      return unknownStep;
    }
  }
}
