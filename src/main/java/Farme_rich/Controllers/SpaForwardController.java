package Farme_rich.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {
/*
    @GetMapping({"/CMS", "/CMS/"})
    public String cmsRoot() {
        return "forward:/CMS/index.html";
    }

    @GetMapping("/CMS/{path:[^.]*}")
    public String cms() {
        return "forward:/CMS/index.html";
    }

    @GetMapping({"/HMS", "/HMS/"})
    public String hmsRoot() {
        return "forward:/HMS/index.html";
    }

    @GetMapping("/HMS/{path:[^.]*}")
    public String hms() {
        return "forward:/HMS/index.html";
    }*/

    @GetMapping({"/wiseGrocer", "/wiseGrocer/"})
    public String wiseGrocerRoot() {
        return "forward:/wiseGrocer/index.html";
    }

    @GetMapping("/wiseGrocer/{path:[^.]*}")
    public String wiseGrocer() {
        return "forward:/wiseGrocer/index.html";
    }

    @GetMapping({"/dolphin-naturals", "/dolphin-naturals/"})
    public String dolphinNaturalsRoot() {
        return "forward:/dolphin-naturals/index.html";
    }

    @GetMapping("/dolphin-naturals/{path:[^.]*}")
    public String dolphinNaturals() {
        return "forward:/dolphin-naturals/index.html";
    }

    @GetMapping({"/purest", "/purest/"})
    public String purestRoot() {
        return "forward:/purest/index.html";
    }

    @GetMapping("/purest/{path:[^.]*}")
    public String purest() {
        return "forward:/purest/index.html";
    }


}