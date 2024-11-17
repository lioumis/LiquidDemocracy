package gr.upatras.ceid.ld.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

    private static final String INDEX = "forward:/app/index.html";

    @RequestMapping("/")
    public String index() {
        return INDEX;
    }

    @RequestMapping("/app")
    public String index1() {
        return INDEX;
    }

    @RequestMapping("/app/")
    public String index2() {
        return INDEX;
    }
}