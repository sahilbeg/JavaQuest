import { useCustomer } from "./CustomerContext.tsx";

/** The greeting in the middle of the page. */
export function Greeting() {
  const { customer } = useCustomer();
  // TODO 3: with no customer, show "Namaste! Sign in to see your orders."
  //         With a customer, show "Namaste, Asha!" using the first word of customer.name.
  return <p>Namaste!</p>;
}
