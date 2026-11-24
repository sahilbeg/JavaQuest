import { useEffect, useRef, useState } from "react";
import { formatRupees } from "./money.js";
import { cartTotals } from "./pricing.js";

const STEPS = ["Address", "Payment", "Done"];
const TITLES = { Address: "Delivery address", Payment: "Pay by UPI", Done: "Thank you!" };

/** The first problem with a delivery address, or "" when it is fine. */
export function addressProblem({ name, phone, pin }) {
  if (name.trim() === "") return "Enter your name.";
  if (!/^\d{10}$/.test(phone.replace(/\s/g, ""))) return "Enter a 10-digit phone number.";
  if (!/^\d{6}$/.test(pin.trim())) return "Enter a 6-digit PIN code.";
  return "";
}

/** True for a UPI ID like meera@okaxis: a name, an @, and the bank's handle. */
export function isValidUpiId(text) {
  return /^[a-z0-9._-]{2,}@[a-z]{2,}$/i.test(text.trim());
}

/** Checkout in three steps: the address, the UPI payment, and the thank-you. */
export function Checkout({ lines, orderId, onPlaced, onBack }) {
  const [step, setStep] = useState("Address");
  const [address, setAddress] = useState({ name: "", phone: "", pin: "" });
  const [upiId, setUpiId] = useState("");
  const [error, setError] = useState("");
  const [paidPaise, setPaidPaise] = useState(0);
  const headingRef = useRef(null);
  const { total } = cartTotals(lines);

  useEffect(() => {
    headingRef.current.focus();
  }, [step]);

  function continueToPayment(event) {
    event.preventDefault();
    const problem = addressProblem(address);
    setError(problem);
    if (problem === "") setStep("Payment");
  }

  function pay(event) {
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

  const field = (key) => ({
    value: address[key],
    onChange: (event) => setAddress({ ...address, [key]: event.target.value }),
  });

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
      {error && (
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
