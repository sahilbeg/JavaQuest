package dev.javaquest.lesson;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Turns a lesson's Markdown into HTML. Fenced blocks with the info strings from CONTENT_SPEC.md become
 * runnable snippets, Mermaid diagrams or callouts. Headings get ids so quiz feedback can link to a section.
 */
@Component
public class LessonRenderer {

    public static final List<String> CALLOUT_KINDS = List.of("tip", "warning", "analogy", "jargon");

    private final Parser parser;
    private final HtmlRenderer renderer;

    public LessonRenderer() {
        var options = new MutableDataSet()
                .set(Parser.EXTENSIONS, List.of(TablesExtension.create(), new SpecialBlocks()))
                .set(HtmlRenderer.GENERATE_HEADER_ID, true)
                .set(HtmlRenderer.RENDER_HEADER_ID, true)
                .set(HtmlRenderer.ESCAPE_HTML, true);
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }

    public String render(String markdown) {
        return renderer.render(parser.parse(markdown));
    }

    private final class SpecialBlocks implements HtmlRenderer.HtmlRendererExtension, Parser.ParserExtension {
        @Override
        public void rendererOptions(MutableDataHolder options) {}

        @Override
        public void extend(HtmlRenderer.Builder builder, String rendererType) {
            builder.nodeRendererFactory(options -> new FencedBlockRenderer());
        }

        @Override
        public void parserOptions(MutableDataHolder options) {}

        @Override
        public void extend(Parser.Builder builder) {}
    }

    private final class FencedBlockRenderer implements NodeRenderer {
        @Override
        public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
            return Set.of(new NodeRenderingHandler<>(FencedCodeBlock.class, this::render));
        }

        private void render(FencedCodeBlock block, NodeRendererContext context, HtmlWriter html) {
            String[] info = block.getInfo().toString().strip().split("\\s+");
            String code = block.getContentChars().normalizeEOL();
            String language = info[0];
            String modifier = info.length > 1 ? info[1] : "";
            html.line();
            switch (language) {
                case "mermaid" -> {
                    html.attr("class", "mermaid").withAttr().tag("pre").openPre();
                    html.text(code).closePre().closeTag("pre");
                }
                case "callout" -> {
                    String kind = CALLOUT_KINDS.contains(modifier) ? modifier : "tip";
                    html.attr("class", "callout callout-" + kind).attr("data-kind", kind).withAttr().tag("aside");
                    html.raw(LessonRenderer.this.render(code));
                    html.closeTag("aside");
                }
                default -> {
                    boolean runnable = language.equals("java") && modifier.equals("run");
                    if (runnable) {
                        html.attr("class", "snippet").attr("data-runnable", "").withAttr().tag("div");
                    }
                    html.tag("pre").openPre();
                    if (!language.isEmpty()) {
                        html.attr("class", "language-" + language).attr("data-lang", language);
                    }
                    html.withAttr().tag("code").text(code).closeTag("code").closePre().closeTag("pre");
                    if (runnable) {
                        html.closeTag("div");
                    }
                }
            }
            html.line();
        }
    }
}
