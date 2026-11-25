import { useCustomer } from "./CustomerContext.tsx";

/** The greeting in the middle of the page. */
export function Greeting() {
  const { customer } = useCustomer();
  if (customer === null) return <p>Namaste! Sign in to see your orders.</p>;
  const firstName = customer.name.split(" ")[0];
  return <p>Namaste, {firstName}!</p>;
}
