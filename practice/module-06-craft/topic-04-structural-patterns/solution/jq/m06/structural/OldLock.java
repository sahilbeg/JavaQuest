package jq.m06.structural;

// Comes from the second-hand fleet. Do not change this class.
// Every method returns 0 when it worked, and another number as an error code.
public class OldLock {

    public int engage(String id) {
        return 0;
    }

    public int disengage(String id) {
        if (id.startsWith("B-99")) {
            return 4;
        }
        return 0;
    }
}
