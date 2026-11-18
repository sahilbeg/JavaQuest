# Annotations and a tiny processor

Replace the hand-written kiosk checks with two annotations and one `Validator` that reads them. `KioskSettings` is already marked with `@NotBlank` and `@Range`. Leave it as it is.

1. **TODO 1 and 2:** give `@NotBlank` and `@Range` a `@Retention(RetentionPolicy.RUNTIME)`. Without it, reflection cannot see them while the program runs.
2. **TODO 3:** in `Validator.validate`, loop over the declared fields of the object and read each value.
3. **TODO 4:** for a field with `@NotBlank`, add `stationName must not be blank` (with the real field name) when the value is null or blank.
4. **TODO 5:** for a field with `@Range`, add `docks must be between 1 and 12` (with the real name, min and max) when the number is outside the range.

Keep the messages in the order the fields are declared. A valid object gives an empty list.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
