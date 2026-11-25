import type { ComponentPropsWithoutRef } from "react";

export type ButtonVariant = "primary" | "secondary" | "danger";
export type ButtonSize = "sm" | "md";

export type ButtonProps = ComponentPropsWithoutRef<"button"> & {
  variant?: ButtonVariant;
  size?: ButtonSize;
};

// TODO 1: Take variant (default "primary"), size (default "md"), className and type (default "button")
//         from the props, and collect everything else with ...rest.
export function Button({ children }: ButtonProps) {
  // TODO 2: Build the classes: "btn", `btn--${variant}`, `btn--${size}`, then className if one was passed.
  //         ["btn", ..., className].filter(Boolean).join(" ")
  // TODO 3: Return <button type={type} className={classes} {...rest} />.
  return <button className="btn">{children}</button>;
}
