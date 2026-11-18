package dev.javaquest.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The page routes: each answers with the single page, which then draws the right view from the path. So
 * /t/module-01-basics/topic-01-variables can be bookmarked, reloaded and opened in a new tab.
 */
@Controller
public class PageController {

    @GetMapping({"/t/{module}/{topic}", "/review/{module}/{topic}", "/notes", "/notes/{module}/{topic}", "/career", "/offer"})
    public String page() {
        return "forward:/index.html";
    }
}
