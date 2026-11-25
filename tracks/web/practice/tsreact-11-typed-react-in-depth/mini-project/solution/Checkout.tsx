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
  const [state, dispatch] = useReducer(checkoutReducer, START);

  const totals = checkoutTotals(lines);

  switch (state.step) {
    case "cart":
      return <CartStep lines={lines} totals={totals} onContinue={() => dispatch({ type: "cartConfirmed" })} />;
    case "address":
      return (
        <AddressStep
          initial={state.saved}
          onSave={(address) => dispatch({ type: "addressSaved", address })}
          onBack={() => dispatch({ type: "wentBack" })}
        />
      );
    case "upi": {
      const address = state.address;
      return (
        <UpiStep
          total={totals.total}
          address={address}
          onBack={() => dispatch({ type: "wentBack" })}
          onPaid={() => {
            const orderId = placeOrder({ lines, address, total: totals.total });
            dispatch({ type: "paid", orderId });
          }}
        />
      );
    }
    case "done":
      return <DoneStep orderId={state.orderId} />;
    default: {
      const unknownStep: never = state;
      return unknownStep;
    }
  }
}
