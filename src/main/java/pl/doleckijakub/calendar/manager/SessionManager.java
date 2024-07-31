package pl.doleckijakub.calendar.manager;

import jakarta.servlet.http.HttpServletRequest;
import pl.doleckijakub.calendar.model.User;

import java.util.Optional;

public class SessionManager {

    private static final String FIELD_USER = "user";

    public static void clear(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    public static Optional<User> getUser(HttpServletRequest request) {
        return Optional.ofNullable((User) request.getSession().getAttribute(FIELD_USER));
    }

    public static void setUser(HttpServletRequest request, User user) {
        request.getSession(true).setAttribute(FIELD_USER, user);
    }

}
