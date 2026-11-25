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

// TODO 1 (bronze): the customer never reaches the gift step, and its heading is wrong.
// TODO 2 (silver): Back must retrace the same steps, and the extras must reach the total.
// TODO 3 (gold): make a forgotten step or action impossible to miss, and don't charge for a name of only spaces.

/** The steps in the order a customer walks through them. */
const ORDER: Step[] = ["cart", "address", "upi", "done"];

/** Where Back goes from each step. Steps with no entry stay where they are. */
const PREVIOUS: Partial<Record<Step, Step>> = { address: "cart", upi: "address" };

export function checkoutReducer(state: CheckoutState, action: CheckoutAction): CheckoutState {
  switch (action.type) {
    case "next": {
      const i = ORDER.indexOf(state.step);
      return i >= 0 && i < ORDER.length - 1 ? { ...state, step: ORDER[i + 1] } : state;
    }
    case "back": {
      const to = PREVIOUS[state.step];
      return to === undefined ? state : { ...state, step: to };
    }
    case "giftChosen":
      return { ...state, giftWrap: action.giftWrap };
    default:
      return state;
  }
}

/** Gift wrap is ₹30 and a stitched name ₹50, in paise. */
export function extrasPaise(state: CheckoutState): number {
  return (state.giftWrap ? 3000 : 0) + (state.stitchedName !== "" ? 5000 : 0);
}

/** The heading for each step. */
export function stepTitle(step: Step): string {
  switch (step) {
    case "cart":
      return "Your cart";
    case "address":
      return "Delivery address";
    case "upi":
      return "Pay by UPI";
    case "done":
      return "Order placed";
    default:
      return "Your cart";
  }
}
