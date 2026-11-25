import { useEffect, useRef, useState, type ChangeEvent, type FormEvent } from "react";
import { formatRupees } from "./money.ts";
import { cartTotals } from "./pricing.ts";
import type { Address, CartLine, PlacedOrder, Step } from "./types.ts";

const STEPS: readonly Step[] = ["Address", "Payment", "Done"];
const TITLES: Record<Step, string> = { Address: "Delivery address", Payment: "Pay by UPI", Done: "Thank you!" };

/** The first problem with a delivery address, or "" when it is fine. */
export function addressProblem({ name, phone, pin }: Address): string {
  // TODO 6: check in this order. An empty name (after trim) gives "Enter your name.".
  // A phone that isn't 10 digits once spaces are removed gives "Enter a 10-digit phone number.".
  // A PIN code that isn't 6 digits gives "Enter a 6-digit PIN code.". A good address gives "".
  return "";
}

/** True for a UPI ID like meera@okaxis: a name, an @, and the bank's handle. */
export function isValidUpiId(text: string): boolean {
  // TODO 7: at least two letters, digits, dots, dashes or underscores, then @, then at least two letters. Ignore capitals and spaces around it.
  return false;
}

export interface CheckoutProps {
  lines: readonly CartLine[];
  orderId: string;
  onPlaced: (order: PlacedOrder) => void;
  onBack: () => void;
}

/** Checkout in three steps: the address, the UPI payment, and the thank-you. */
export function Checkout({ lines, orderId, onPlaced, onBack }: CheckoutProps) {
  const [step, setStep] = useState<Step>("Address");
  const [address, setAddress] = useState<Address>({ name: "", phone: "", pin: "" });
  const [upiId, setUpiId] = useState("");
  const [error, setError] = useState("");
  const [paidPaise, setPaidPaise] = useState(0);
  const headingRef = useRef<HTMLHeadingElement>(null);
  const { total } = cartTotals(lines);

  // TODO 8: when checkout opens, and whenever step changes, move focus to the step's heading (headingRef) with useEffect.

  function continueToPayment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const problem = addressProblem(address);
    setError(problem);
    if (problem === "") setStep("Payment");
  }

  function pay(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!isValidUpiId(upiId)) {
      setError("Enter a UPI ID like name@bank.");
      return;
    }
    setError("");
    setPaidPaise(total);
    setStep("Done");
    onPlaced({ orderId, totalPaise: total, payment: "UPI" });
  }

  function field(key: keyof Address) {
    return {
      value: address[key],
      onChange: (event: ChangeEvent<HTMLInputElement>) => setAddress({ ...address, [key]: event.target.value }),
    };
  }

  return (
    <section className="checkout" aria-labelledby="checkout-title">
      <ol className="steps" aria-label="Checkout steps">
        {STEPS.map((name) => (
          <li key={name} aria-current={name === step ? "step" : undefined}>
            {name}
          </li>
        ))}
      </ol>
      <h2 id="checkout-title" tabIndex={-1} ref={headingRef}>
        {TITLES[step]}
      </h2>
      {error !== "" && (
        <p role="alert" className="error">
          {error}
        </p>
      )}

      {step === "Address" && (
        <form onSubmit={continueToPayment} noValidate>
          <label htmlFor="name">Name</label>
          <input id="name" autoComplete="name" {...field("name")} />
          <label htmlFor="phone">Phone</label>
          <input id="phone" type="tel" autoComplete="tel" {...field("phone")} />
          <label htmlFor="pin">PIN code</label>
          <input id="pin" inputMode="numeric" autoComplete="postal-code" {...field("pin")} />
          <div className="actions">
            <button type="button" onClick={onBack}>
              Back to the shop
            </button>
            <button type="submit">Continue to payment</button>
          </div>
        </form>
      )}

      {step === "Payment" && (
        <form onSubmit={pay} noValidate>
          <p>Amount to pay: {formatRupees(total)}</p>
          <label htmlFor="upi">UPI ID</label>
          <input id="upi" autoComplete="off" value={upiId} onChange={(event) => setUpiId(event.target.value)} />
          <button type="submit">Pay {formatRupees(total)}</button>
        </form>
      )}

      {step === "Done" && (
        <div>
          <p>Order {orderId} is placed.</p>
          <p>Paid {formatRupees(paidPaise)} by UPI.</p>
          <button type="button" onClick={onBack}>
            Back to the shop
          </button>
        </div>
      )}
    </section>
  );
}
