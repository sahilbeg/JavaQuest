package fx;

import org.junit.jupiter.api.Test;

class ChattyTest {
    @Test
    void printsALot() {
        for (int i = 0; i < 200_000; i++) {
            System.out.println("line " + i);
        }
    }
}
