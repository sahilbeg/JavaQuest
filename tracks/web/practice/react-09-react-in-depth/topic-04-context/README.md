# Context: sharing state without passing props

Share one cart with the whole shop, so the header, the Add buttons and the total all agree.

`src/useCart.js` (the hook from the last ticket), `src/Shop.jsx`, `src/items.js` and `src/money.js` are done.

1. **TODO 1** in `src/CartContext.jsx`: `CartProvider` calls `useCart()` once and shares the result through `CartContext` with its `children`.
2. **TODO 2** in `src/CartContext.jsx`: `useCartContext()` reads the cart with `useContext`. If there's no provider above (the value is `null`), it throws an `Error` saying `useCartContext must be used inside <CartProvider>`.
3. **TODO 3** in `src/Header.jsx`: show `Cart (n)` with the shared cart's `count`.
4. **TODO 4** in `src/AddButton.jsx`: pressing `Add Keychain` adds that item to the shared cart.
5. **TODO 5** in `src/CartTotal.jsx`: show `Total: ₹390.00` with the shared cart's `total`.

No component passes the cart as a prop. Each one reads it with `useCartContext()`.
