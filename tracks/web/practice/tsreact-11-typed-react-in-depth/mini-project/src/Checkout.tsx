import { useReducer } from "react";
import type { Dispatch } from "react";
import { START, checkoutReducer } from "./checkoutMachine.ts";
import type { CheckoutAction, CheckoutState } from "./checkoutMachine.ts";
import type { Address } from "./address.ts";
import { AddressStep, CartStep, DoneStep, UpiStep } from "./steps.tsx";
import { checkoutTotals } from "./totals.ts";
import type { CartLine } from "./totals.ts";

export interface PlacedOrder {
  lines: CartLine[];
  address: Address;
  total: number;
}

export interface CheckoutProps {
  lines: CartLine[];
  /** Places the order with the server and returns its id, like "order-17". */
  placeOrder: (order: PlacedOrder) => string;
}

export function Checkout({ lines, placeOrder }: CheckoutProps) {
  // TODO 8: replace these two lines with const [state, dispatch] = useReducer(checkoutReducer, START);
  const state: CheckoutState = START;
  const dispatch: Dispatch<CheckoutAction> = () => {};

  const totals = checkoutTotals(lines);

  // TODO 9: switch (state.step) and return one step component per case:
  //   cart    -> <CartStep lines={lines} totals={totals} onContinue={...cartConfirmed} />
  //   address -> <AddressStep initial={state.saved} onSave={(address) => ...addressSaved} onBack={...wentBack} />
  //   upi     -> <UpiStep total={totals.total} address={state.address} onPaid={...} onBack={...wentBack} />
  //   done    -> <DoneStep orderId={state.orderId} />
  //   default -> const unknownStep: never = state; return unknownStep;
  // TODO 10: UpiStep's onPaid calls placeOrder({ lines, address: state.address, total: totals.total }) once,
  //          then dispatches { type: "paid", orderId } with the id it returned.
  return <CartStep lines={lines} totals={totals} onContinue={() => dispatch({ type: "cartConfirmed" })} />;
}
