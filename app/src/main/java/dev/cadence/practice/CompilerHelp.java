package dev.cadence.practice;

import dev.cadence.runner.TestRunResult.CompileError;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Explains common javac errors in words a beginner can act on. Unknown errors get no explanation. */
public final class CompilerHelp {

    private record Rule(Pattern pattern, Function<Matcher, String> explanation) {}

    private static final List<Rule> RULES = List.of(
            rule("^'(.)' expected", m -> "Java expected a " + m.group(1) + " here. Check the end of this line or the one before it."),
            rule("^cannot find symbol", m -> "Java doesn't know this name. Check the spelling and the capital letters, and make sure you declared it first."),
            rule("^incompatible types: (.+) cannot be converted to (.+)", m -> "Wrong type: a " + m.group(1) + " can't go where a " + m.group(2) + " is needed."),
            rule("^incompatible types: possible lossy conversion from (\\S+) to (\\S+)", m -> "Turning a " + m.group(1) + " into a " + m.group(2) + " could lose part of the value. Add a cast like (" + m.group(2) + ") if you mean it."),
            rule("^missing return statement", m -> "This method promises to return a value, but Java can reach its end without a return."),
            rule("^unclosed string literal", m -> "A piece of text is missing its closing double quote \"."),
            rule("^reached end of file while parsing", m -> "A closing curly brace } is missing. Every { needs a matching }."),
            rule("^class, interface, enum, or record expected", m -> "There is code outside the class. Often this means an extra } closed the class too early."),
            rule("^variable (\\w+) might not have been initialized", m -> m.group(1) + " is used before it has a value. Give it a starting value when you declare it."),
            rule("^variable (\\w+) is already defined", m -> "You declared " + m.group(1) + " twice. Use a new name or remove the second declaration."),
            rule("^package (\\S+) does not exist", m -> "Java can't find " + m.group(1) + ". Check the capital letters: System, not system."),
            rule("^not a statement", m -> "This line isn't a complete instruction. Did you forget an = or a method call?"),
            rule("^unreachable statement", m -> "This line can never run, because a return, break or endless loop comes before it."),
            rule("^non-static (method|variable) (\\S+) cannot be referenced from a static context",
                    m -> "You used " + m.group(2) + " from static code such as main. Create an object first, or make it static."),
            rule("^illegal start of expression", m -> "Java got confused at this spot. Look for a missing bracket, brace or semicolon just before it."));

    private CompilerHelp() {}

    public static Optional<String> explain(String javacMessage) {
        String firstLine = javacMessage.lines().findFirst().orElse("").strip();
        for (Rule rule : RULES) {
            Matcher matcher = rule.pattern().matcher(firstLine);
            if (matcher.find()) {
                return Optional.of(rule.explanation().apply(matcher));
            }
        }
        return Optional.empty();
    }

    public static List<String> explainAll(List<CompileError> errors) {
        return errors.stream().map(e -> explain(e.message()).orElse("")).toList();
    }

    private static Rule rule(String regex, Function<Matcher, String> explanation) {
        return new Rule(Pattern.compile(regex), explanation);
    }
}
