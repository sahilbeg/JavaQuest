import { useCustomer } from "./CustomerContext.tsx";

/** The button in the header. */
export function AccountButton() {
  const { customer, signIn, signOut } = useCustomer();
  if (customer) return <button onClick={signOut}>Sign out</button>;
  return <button onClick={() => signIn({ name: "Asha Rao", phone: "9876543210" })}>Sign in as Asha</button>;
}
