package jq.m06.behavioral;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandHistory {
    private final Deque<AdminCommand> done = new ArrayDeque<>();

    public void run(AdminCommand command) {
        command.execute();
        done.push(command);
    }

    public boolean undo() {
        if (done.isEmpty()) {
            return false;
        }
        done.pop().undo();
        return true;
    }

    public int size() {
        return done.size();
    }
}
