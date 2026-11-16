package jq.m08.tx;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Moves ride credit from one rider's wallet to a friend's wallet. */
@Service
public class GiftService {

    private final WalletRepository wallets;
    private final GiftLogRepository giftLogs;

    public GiftService(WalletRepository wallets, GiftLogRepository giftLogs) {
        this.wallets = wallets;
        this.giftLogs = giftLogs;
    }

    @Transactional(readOnly = true)
    public long balanceOf(String riderId) {
        return wallets.findById(riderId).orElseThrow(() -> new UnknownRiderException(riderId)).getBalanceCents();
    }

    /** Takes cents from one wallet and puts them in another, then writes a GiftLog. All or nothing. */
    // TODO 1: make gift one transaction that also rolls back on the checked InsufficientCreditException
    public void gift(String fromRider, String toRider, long cents) throws InsufficientCreditException {
        if (cents <= 0) {
            throw new IllegalArgumentException("a gift must be at least 1 cent, not " + cents);
        }
        Wallet sender = wallets.findById(fromRider).orElseThrow(() -> new UnknownRiderException(fromRider));
        if (sender.getBalanceCents() < cents) {
            throw new InsufficientCreditException(fromRider, cents, sender.getBalanceCents());
        }

        // Step 1: withdraw from the sender
        sender.withdraw(cents);
        wallets.save(sender);

        // Step 2: deposit to the friend. An unknown friend fails here, after step 1.
        Wallet receiver = wallets.findById(toRider).orElseThrow(() -> new UnknownRiderException(toRider));
        receiver.deposit(cents);
        wallets.save(receiver);

        // Step 3: keep a record for finance
        giftLogs.save(new GiftLog(fromRider, toRider, cents));
    }

    /** Gives the same amount to every friend in the list. All or nothing: either every friend gets it, or nobody does. */
    // TODO 2: gift(...) below is called through 'this', so its @Transactional is skipped: give giftToEach its own, same rule
    public void giftToEach(String fromRider, List<String> friends, long centsEach) throws InsufficientCreditException {
        for (String friend : friends) {
            gift(fromRider, friend, centsEach);
        }
    }
}
