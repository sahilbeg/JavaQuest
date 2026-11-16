package jq.m08.tx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// No @Transactional on this class on purpose: every repository call below runs in its own,
// fresh transaction, so the tests see only what GiftService really committed.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class GiftServiceTest {

    static final String ADA = "r-17";
    static final String LIN = "r-21";
    static final String OMAR = "r-33";
    static final String ZOE = "r-40";
    static final String NOBODY = "r-99";   // has no wallet

    @Autowired GiftService giftService;
    @Autowired WalletRepository wallets;
    @Autowired GiftLogRepository giftLogs;

    @BeforeEach
    void freshWallets() {
        giftLogs.deleteAll();
        wallets.deleteAll();
        wallets.save(new Wallet(ADA, 2000));
        wallets.save(new Wallet(LIN, 100));
        wallets.save(new Wallet(OMAR, 0));
        wallets.save(new Wallet(ZOE, 50));
    }

    long balance(String riderId) {
        return wallets.findById(riderId).orElseThrow().getBalanceCents();
    }

    @Test
    @DisplayName("A good gift moves 500 cents from Ada to Lin and writes one GiftLog")
    void goodGiftMovesMoney() throws Exception {
        giftService.gift(ADA, LIN, 500);

        assertEquals(1500, balance(ADA), "Ada had 2000 cents and gave 500, so she should have 1500");
        assertEquals(600, balance(LIN), "Lin had 100 cents and got 500, so she should have 600");
        List<GiftLog> logs = giftLogs.findAll();
        assertEquals(1, logs.size(), "one good gift should write exactly one GiftLog row");
        assertEquals(500, logs.get(0).getCents(), "the GiftLog should record the 500 cents that moved");
    }

    @Test
    @DisplayName("A gift to a rider with no wallet fails, and Ada keeps every cent")
    void giftToUnknownRiderChangesNothing() {
        assertThrows(UnknownRiderException.class, () -> giftService.gift(ADA, NOBODY, 500),
                "a gift to a rider with no wallet should throw UnknownRiderException");

        assertEquals(2000, balance(ADA),
                "the gift failed, so Ada should still have all 2000 cents. If she has 1500, the withdrawal "
                        + "committed on its own: gift needs @Transactional so both steps commit or roll back together");
        assertEquals(0, giftLogs.count(), "a failed gift should leave no GiftLog behind");
    }

    @Test
    @DisplayName("Gifting to each friend is all or nothing when one friend has no wallet")
    void giftToEachRollsBackOnUnknownFriend() {
        assertThrows(UnknownRiderException.class, () -> giftService.giftToEach(ADA, List.of(LIN, NOBODY, OMAR), 300),
                "giftToEach should throw UnknownRiderException when one friend has no wallet");

        assertEquals(2000, balance(ADA),
                "the whole giftToEach failed, so Ada should still have 2000 cents. giftToEach calls gift through "
                        + "'this', which skips Spring's proxy, so giftToEach needs its own @Transactional");
        assertEquals(100, balance(LIN),
                "the whole giftToEach failed, so Lin should not keep the 300 cents from the first gift");
        assertEquals(0, giftLogs.count(), "a failed giftToEach should leave no GiftLog behind");
    }

    @Test
    @DisplayName("Gifting to each friend is all or nothing when Ada runs out of credit halfway")
    void giftToEachRollsBackOnCheckedException() {
        assertThrows(InsufficientCreditException.class,
                () -> giftService.giftToEach(ADA, List.of(LIN, OMAR, ZOE), 800),
                "Ada has 2000 cents, so the third gift of 800 should throw InsufficientCreditException");

        assertEquals(2000, balance(ADA),
                "the whole giftToEach failed, so Ada should still have 2000 cents. InsufficientCreditException "
                        + "is a checked exception, and Spring commits on those unless the @Transactional on giftToEach says rollbackFor = InsufficientCreditException.class");
        assertEquals(100, balance(LIN), "Lin should not keep the 800 cents from the first gift");
        assertEquals(0, balance(OMAR), "Omar should not keep the 800 cents from the second gift");
        assertEquals(0, giftLogs.count(), "a failed giftToEach should leave no GiftLog behind");
    }
}
