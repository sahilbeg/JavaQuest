# Typed context

Share the signed-in customer with every part of the page, without passing props through each layer.

In `src/CustomerContext.tsx`, the types and the context are written for you:

1. **TODO 1:** `CustomerProvider` keeps the customer in `useState<Customer | null>(null)` and wraps its children in `<CustomerContext.Provider>` with the value `{ customer, signIn, signOut }`. `signIn(customer)` stores the customer; `signOut()` sets it back to `null`.
2. **TODO 2:** `useCustomer()` reads the context with `useContext`. When there is no provider (the value is `null`), it throws `new Error("useCustomer must be used inside <CustomerProvider>")`. Otherwise it returns the value.

In `src/Greeting.tsx`:

3. **TODO 3:** with nobody signed in, the greeting says `Namaste! Sign in to see your orders.`. With a customer, it says `Namaste, Asha!`, using the first word of the customer's name.

`src/AccountButton.tsx` is done: it shows **Sign in as Asha** or **Sign out**. `src/main.tsx` puts the button and the greeting far apart in the preview.
