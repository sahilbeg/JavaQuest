import { useReducer } from "react";
import type { ChangeEvent, Dispatch } from "react";
import { START, checkoutReducer, extrasPaise, stepTitle } from "./checkout.ts";
import type { CheckoutAction, CheckoutState } from "./checkout.ts";
import { rupees } from "./money.ts";

function GiftOptions({ state, dispatch }: { state: CheckoutState; dispatch: Dispatch<CheckoutAction> }) {
  return (
    <div>
      <label>
        <input
          type="checkbox"
          checked={state.giftWrap}
          onChange={(e: ChangeEvent<HTMLInputElement>) =>
            dispatch({ type: "giftChosen", giftWrap: e.currentTarget.checked, stitchedName: state.stitchedName })
          }
        />{" "}
        Gift wrap (₹30)
      </label>
      <label>
        Name to stitch on (₹50){" "}
        <input
          value={state.stitchedName}
          onChange={(e: ChangeEvent<HTMLInputElement>) =>
            dispatch({ type: "giftChosen", giftWrap: state.giftWrap, stitchedName: e.currentTarget.value })
          }
        />
      </label>
    </div>
  );
}

/** Checkout for a cart whose items come to itemsPaise. */
export function Checkout({ itemsPaise }: { itemsPaise: number }) {
  const [state, dispatch] = useReducer(checkoutReducer, START);
  const total = itemsPaise + extrasPaise(state);

  return (
    <section>
      <h2>{stepTitle(state.step)}</h2>
      {state.step === "cart" && <p>Items: {rupees(itemsPaise)}</p>}
      {state.step === "address" && <p>Delivering to Asha Rao, 411007</p>}
      {state.step === "gift" && <GiftOptions state={state} dispatch={dispatch} />}
      {state.step === "upi" && <p>Pay {rupees(total)} to dhaaga@upi</p>}
      {state.step === "done" && <p>Thank you! Your order is placed.</p>}
      {state.step !== "cart" && state.step !== "done" && <button onClick={() => dispatch({ type: "back" })}>Back</button>}
      {state.step !== "done" && <button onClick={() => dispatch({ type: "next" })}>Continue</button>}
    </section>
  );
}
