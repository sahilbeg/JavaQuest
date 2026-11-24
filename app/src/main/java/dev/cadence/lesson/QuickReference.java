package dev.cadence.lesson;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Quick reference notes for a finished ticket, taken from its lesson so every ticket has them without extra
 * writing: the Recap bullets, each "### Syntax: ..." box with its code and first explanation, the words defined
 * in jargon callouts, and the Common mistakes with their fixes. HTML is escaped like the lesson's.
 */
@Component
public class QuickReference {

    /** @param keyPointsHtml the Recap list as HTML, or empty when the lesson has none */
    public record Notes(String keyPointsHtml, List<Syntax> syntax, List<String> wordsHtml, List<Mistake> mistakes) {

        public boolean isEmpty() {
            return keyPointsHtml.isEmpty() && syntax.isEmpty() && wordsHtml.isEmpty() && mistakes.isEmpty();
        }
    }

    /** @param noteHtml the first paragraph after the code: what it means, in a sentence or two */
    public record Syntax(String titleHtml, String code, String noteHtml) {}

    /** @param code the wrong code, or empty; @param fixHtml what the learner sees and how to fix it */
    public record Mistake(String titleHtml, String code, String fixHtml) {}

    /** The code blocks a Syntax box or a mistake can show: Java, and the web courses' JavaScript, JSX, HTML and CSS. */
    private static final java.util.Set<String> CODE_LANGUAGES = java.util.Set.of("java", "js", "jsx", "html", "css");

    private final Parser parser;
    private final HtmlRenderer renderer;

    public QuickReference() {
        var options = new MutableDataSet()
                .set(Parser.EXTENSIONS, List.of(TablesExtension.create()))
                .set(HtmlRenderer.ESCAPE_HTML, true);
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }

    public Notes notes(String markdown) {
        var collector = new Collector();
        for (Node node = parser.parse(markdown).getFirstChild(); node != null; node = node.getNext()) {
            collector.accept(node);
        }
        collector.flush();
        return new Notes(collector.keyPoints, List.copyOf(collector.syntax), List.copyOf(collector.words),
                List.copyOf(collector.mistakes));
    }

    /** Walks the lesson's top-level blocks once, remembering which section and box it is in. */
    private final class Collector {
        String keyPoints = "";
        final List<Syntax> syntax = new ArrayList<>();
        final List<String> words = new ArrayList<>();
        final List<Mistake> mistakes = new ArrayList<>();

        String section = "";
        String syntaxTitle;
        String syntaxCode;
        String mistakeTitle;
        String mistakeCode = "";
        final StringBuilder mistakeFix = new StringBuilder();

        void accept(Node node) {
            if (node instanceof Heading heading) {
                flush();
                String text = heading.getText().toString().strip();
                if (heading.getLevel() == 2) {
                    section = text;
                } else if (heading.getLevel() == 3 && text.startsWith("Syntax:")) {
                    syntaxTitle = inner(heading, "h3").replaceFirst("^Syntax:\\s*", "");
                }
            } else if (node instanceof FencedCodeBlock block) {
                String[] info = block.getInfo().toString().strip().split("\\s+");
                String code = block.getContentChars().normalizeEOL().stripTrailing();
                if (info[0].equals("callout") && info.length > 1 && info[1].equals("jargon")) {
                    words.add(html(parser.parse(code)));
                } else if (CODE_LANGUAGES.contains(info[0])) {
                    if (syntaxTitle != null && syntaxCode == null) {
                        syntaxCode = code;
                    } else if (mistakeTitle != null && mistakeCode.isEmpty()) {
                        mistakeCode = code;
                    }
                }
            } else if (node instanceof BulletList list && section.equals("Recap") && keyPoints.isEmpty()) {
                keyPoints = html(list);
            } else if (node instanceof Paragraph paragraph) {
                if (syntaxTitle != null && syntaxCode != null) {
                    syntax.add(new Syntax(syntaxTitle, syntaxCode, html(paragraph)));
                    syntaxTitle = null;
                    syntaxCode = null;
                } else if (section.equals("Common mistakes")) {
                    String title = boldTitle(paragraph);
                    if (title != null) {
                        flushMistake();
                        mistakeTitle = title;
                    } else if (mistakeTitle != null) {
                        mistakeFix.append(html(paragraph)).append('\n');
                    }
                }
            }
        }

        void flush() {
            if (syntaxTitle != null && syntaxCode != null) {
                syntax.add(new Syntax(syntaxTitle, syntaxCode, ""));
            }
            syntaxTitle = null;
            syntaxCode = null;
            flushMistake();
        }

        void flushMistake() {
            if (mistakeTitle != null) {
                mistakes.add(new Mistake(mistakeTitle, mistakeCode, mistakeFix.toString().strip()));
            }
            mistakeTitle = null;
            mistakeCode = "";
            mistakeFix.setLength(0);
        }
    }

    /** A Common mistakes title is a paragraph that is only bold text, like "**1. Forgetting the semicolon.**". */
    private String boldTitle(Paragraph paragraph) {
        Node first = paragraph.getFirstChild();
        if (first instanceof StrongEmphasis && first.getNext() == null) {
            return inner(paragraph, "p").replaceFirst("^<strong>", "").replaceFirst("</strong>$", "")
                    .replaceFirst("^\\d+\\.\\s*", "").strip();
        }
        return null;
    }

    /** The node's HTML without its outer tag, so inline code in a title stays code. */
    private String inner(Node node, String tag) {
        String html = html(node).replaceFirst("^<" + tag + "[^>]*>", "").replaceFirst("</" + tag + ">$", "");
        return html.strip();
    }

    private String html(Node node) {
        return renderer.render(node).strip();
    }
}
