package jq.m04.stacksqueues;

import java.util.ArrayDeque;

public class Brackets {

    public static boolean balanced(String text) {
        ArrayDeque<Character> open = new ArrayDeque<>();
        for (char c : text.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                open.push(c);
            } else if (c == ')' || c == ']' || c == '}') {
                if (open.isEmpty()) {
                    return false;
                }
                if (!matches(open.pop(), c)) {
                    return false;
                }
            }
        }
        return open.isEmpty();
    }

    private static boolean matches(char open, char close) {
        return (open == '(' && close == ')')
                || (open == '[' && close == ']')
                || (open == '{' && close == '}');
    }
}
