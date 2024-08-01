package pl.doleckijakub.calendar.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import pl.doleckijakub.calendar.dataaccess.UserDataAccessService;
import pl.doleckijakub.calendar.manager.SessionManager;
import pl.doleckijakub.calendar.model.User;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/user")
public class UserController {

    private final UserDataAccessService userDataAccessService;
    private final Logger logger;

    @Autowired
    public UserController(@Qualifier("postgresUserAS") UserDataAccessService userDataAccessService) {
        this.userDataAccessService = userDataAccessService;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @GetMapping("/me")
    public Optional<User> me(HttpServletRequest request) {
        return SessionManager.getUser(request);
    }

    @PostMapping("/register")
    public void register(HttpServletRequest request, HttpServletResponse response, @RequestParam("username") String username, @RequestParam("password") String password) throws IOException {
        try {
            User user = userDataAccessService.register(username, password);

            SessionManager.setUser(request, user);
            logger.info("{} registered", user.id());

            response.sendRedirect("/");
        } catch (Exception e) {
            response.sendRedirect("/register?error");
        }
    }

    @PostMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response, @RequestParam("username") String username, @RequestParam("password") String password) throws IOException {
        try {
            User user = userDataAccessService.login(username, password);

            SessionManager.setUser(request, user);
            logger.info("{} logged in", user.id());

            response.sendRedirect("/");
        } catch (Exception e) {
            response.sendRedirect("/login?error");
        }
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SessionManager.getUser(request).isEmpty()) return;

        logger.info("{} logged out", SessionManager.getUser(request).get().id());
        SessionManager.clear(request);

        response.sendRedirect("/");
    }

}
