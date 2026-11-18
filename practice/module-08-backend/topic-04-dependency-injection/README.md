# Dependency injection and beans

`FareService` builds its own price list and clock with `new`. Tests cannot pretend it is Sunday, and Jiraiya's half-price Sunday promo never gets used. Let Spring create the pieces and hand them to `FareService`.

The fares, in cents: a ride costs the unlock fee plus the per-minute price times the minutes.

| Price list | Unlock | CLASSIC | EBIKE | CARGO |
|---|---|---|---|---|
| `StandardPriceList` (every day but Sunday) | 100 | 15 | 30 | 25 |
| `SundayPriceList` (Sundays, half price rounded down) | 50 | 7 | 15 | 12 |

The prices are already written. Your work is the wiring.

1. **TODO 1:** put `@Component` and `@Primary` above `StandardPriceList`, so it is a bean and the default price list.
2. **TODO 2:** put `@Component` and `@Qualifier("sunday")` above `SundayPriceList`.
3. **TODO 3:** put `@Configuration` above `ClockConfig` and `@Bean` above its `clock()` method.
4. **TODO 4:** put `@Service` above `FareService`.
5. **TODO 5:** replace the two `new` lines in `FareService` with three fields set by a constructor: `FareService(PriceList standard, @Qualifier("sunday") PriceList sunday, Clock clock)`.
6. **TODO 6:** in `fareCents`, use the `sunday` price list when `LocalDate.now(clock).getDayOfWeek()` is `DayOfWeek.SUNDAY`, and the `standard` one on other days.

Do the TODOs in order. If you add the constructor before `@Primary`, Spring stops at startup because it cannot choose between two price lists.

## How to read the tests

Open `FareServiceTest.java`. `@SpringBootTest` starts your app inside the test, without a web server.

- The test adds its own `TestClock` bean, marked `@Primary`, so Spring hands it to `FareService` instead of the real clock. Each fare test sets it to a Monday or a Sunday morning.
- Some tests ask the `ApplicationContext` which beans exist, for example how many `PriceList` beans there are and which one is primary.
- One test asks for `FareService` twice and checks it is the same object, because beans are singletons.

Press **Submit** in Cadence, or just save a file in your IDE. The tests run on their own.
