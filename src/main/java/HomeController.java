
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // =========================
    // LANDING PAGE
    // =========================
    @GetMapping("/")
    public String homePage() {

        // Opens home.html
        return "home";
    }
}
