package jq.m06.behavioral;

public class CommandHistory {
    // TODO 9: keep a stack of commands that have run

    public void run(AdminCommand command) {
        // TODO 10: execute the command and push it on the stack
    }

    public boolean undo() {
        // TODO 11: pop the newest command and undo it; return false when there is nothing to undo
        return false;
    }

    public int size() {
        // TODO 12: return how many commands can still be undone
        return 0;
    }
}
