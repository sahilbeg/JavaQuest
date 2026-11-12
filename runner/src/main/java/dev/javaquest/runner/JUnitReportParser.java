package dev.javaquest.runner;

import dev.javaquest.runner.TestRunResult.TestCaseResult;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Reads the legacy XML report that the JUnit Platform console launcher writes for the Jupiter engine. */
public final class JUnitReportParser {

    private static final String DISPLAY_NAME_PREFIX = "display-name: ";
    private static final String DISPLAY_NAME_SEPARATOR = " > ";

    private JUnitReportParser() {}

    public static List<TestCaseResult> parse(InputStream xml) throws IOException {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            var document = factory.newDocumentBuilder().parse(xml);
            NodeList cases = document.getElementsByTagName("testcase");
            var results = new ArrayList<TestCaseResult>();
            for (int i = 0; i < cases.getLength(); i++) {
                results.add(toResult((Element) cases.item(i)));
            }
            return results;
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("JUnit report is not valid XML", e);
        }
    }

    private static TestCaseResult toResult(Element testCase) {
        String name = displayName(testCase);
        Element problem = firstChild(testCase, "failure");
        if (problem == null) {
            problem = firstChild(testCase, "error");
        }
        if (problem == null) {
            return new TestCaseResult(name, true, "");
        }
        String message = problem.getAttribute("message");
        if ("error".equals(problem.getTagName())) {
            message = simpleName(problem.getAttribute("type")) + (message.isEmpty() ? "" : ": " + message);
        }
        return new TestCaseResult(name, false, message);
    }

    private static String displayName(Element testCase) {
        Element out = firstChild(testCase, "system-out");
        if (out != null) {
            for (String line : out.getTextContent().split("\n")) {
                if (line.startsWith(DISPLAY_NAME_PREFIX)) {
                    String path = line.substring(DISPLAY_NAME_PREFIX.length()).strip();
                    int separator = path.lastIndexOf(DISPLAY_NAME_SEPARATOR);
                    return separator < 0 ? path : path.substring(separator + DISPLAY_NAME_SEPARATOR.length());
                }
            }
        }
        return testCase.getAttribute("name");
    }

    private static Element firstChild(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
    }

    private static String simpleName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }
}
