package jq.m06.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class CheckoutTest {

    private static final String CARD = "4242 4242 4242 4242";
    private static final String MASKED = "**** **** **** 4242";

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        logger = (Logger) LoggerFactory.getLogger(Checkout.class);
        logger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
        logger.setLevel(null);
    }

    private ILoggingEvent onlyEventAt(Level level) {
        ILoggingEvent found = null;
        int count = 0;
        for (ILoggingEvent event : appender.list) {
            if (event.getLevel() == level) {
                found = event;
                count++;
            }
        }
        if (count > 1) {
            fail("expected one " + level + " log line but " + count + " were recorded; log each outcome once");
        }
        if (found == null) {
            fail("no " + level + " log line was recorded; use the SLF4J logger from "
                    + "LoggerFactory.getLogger(Checkout.class), not System.out.println");
        }
        return found;
    }

    @Test
    @DisplayName("mask keeps only the last 4 digits of a card number")
    void maskShowsLastFour() {
        assertEquals(MASKED, PaymentLog.mask(CARD),
                "mask(\"4242 4242 4242 4242\") should be \"**** **** **** 4242\"");
        assertEquals("**** **** **** 5556", PaymentLog.mask("4000056655665556"),
                "mask(\"4000056655665556\") should be \"**** **** **** 5556\", even without spaces");
        assertEquals("****", PaymentLog.mask("12"),
                "mask(\"12\") should be \"****\": fewer than 4 digits shows no digits at all");
    }

    @Test
    @DisplayName("A successful payment logs one INFO line with {} placeholders")
    void successLogsInfo() {
        assertTrue(new Checkout().pay("R-17", CARD, 1250), "pay should return true for 1250 cents");
        ILoggingEvent event = onlyEventAt(Level.INFO);
        assertEquals("payment ok for R-17 card " + MASKED + " amount 1250", event.getFormattedMessage(),
                "the INFO line should read: payment ok for R-17 card " + MASKED + " amount 1250");
        assertEquals("payment ok for {} card {} amount {}", event.getMessage(),
                "build the INFO line with {} placeholders, not with + string joining");
    }

    @Test
    @DisplayName("A payment over 50,000 cents is declined with a WARN line")
    void declineLogsWarn() {
        assertFalse(new Checkout().pay("R-17", CARD, 60_000), "pay should return false for 60,000 cents");
        ILoggingEvent event = onlyEventAt(Level.WARN);
        assertEquals("payment declined for R-17 card " + MASKED, event.getFormattedMessage(),
                "the WARN line should read: payment declined for R-17 card " + MASKED);
    }

    @Test
    @DisplayName("A missing card number logs ERROR with the exception attached")
    void nullCardLogsError() {
        assertFalse(new Checkout().pay("R-17", null, 1250), "pay should return false when the card number is null");
        ILoggingEvent event = onlyEventAt(Level.ERROR);
        assertEquals("payment failed for R-17", event.getFormattedMessage(),
                "the ERROR line should read: payment failed for R-17");
        IThrowableProxy thrown = event.getThrowableProxy();
        assertNotNull(thrown, "attach the exception: pass new IllegalArgumentException(...) as the last argument to log.error");
        assertEquals(IllegalArgumentException.class.getName(), thrown.getClassName(),
                "the attached exception should be an IllegalArgumentException");
    }

    @Test
    @DisplayName("No log line ever contains the full card number")
    void noFullCardInLogs() {
        Checkout checkout = new Checkout();
        checkout.pay("R-17", CARD, 1250);
        checkout.pay("R-17", CARD, 60_000);
        long infoAndWarn = appender.list.stream()
                .filter(event -> event.getLevel() == Level.INFO || event.getLevel() == Level.WARN)
                .count();
        assertEquals(2, infoAndWarn,
                "two payments should record one INFO line and one WARN line; use the SLF4J logger, not System.out.println");
        for (ILoggingEvent event : appender.list) {
            String line = event.getFormattedMessage();
            assertFalse(line.contains(CARD) || line.contains("4242424242424242"),
                    "this log line leaks the full card number, mask it first: " + line);
        }
    }
}
