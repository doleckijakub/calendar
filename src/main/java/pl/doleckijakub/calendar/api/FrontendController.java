package pl.doleckijakub.calendar.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.doleckijakub.calendar.manager.SessionManager;

import java.io.IOException;

@Controller
@RequestMapping("/")
public class FrontendController {

    @GetMapping("/")
    public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(SessionManager.getUser(request).isEmpty() ? "/login" : "/calendar");
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        model.addAttribute("error", request.getParameter("error") == null ? null : "Failed to log in. Username or password incorrect.");
        return "login";
    }

    @GetMapping("/register")
    public String register(HttpServletRequest request, Model model) {
        model.addAttribute("error", request.getParameter("error") == null ? null : "Failed to register. User with this username already exists.");
        return "register";
    }

    @GetMapping("/calendar")
    public String calendar(Model model) {
        return "calendar";
    }

}
