package pl.doleckijakub.calendar.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.doleckijakub.calendar.dataaccess.EventDataAccessService;
import pl.doleckijakub.calendar.manager.SessionManager;
import pl.doleckijakub.calendar.model.Event;
import pl.doleckijakub.calendar.model.User;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/")
public class FrontendController {

    private static final int DAYS_TO_SHOW = 7;

    private final EventDataAccessService eventDataAccessService;

    @Autowired
    public FrontendController(@Qualifier("postgresEventAS") EventDataAccessService eventDataAccessService) {
        this.eventDataAccessService = eventDataAccessService;
    }

    @GetMapping("/")
    public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(SessionManager.getUser(request).isEmpty() ? "/login" : "/calendar");
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        if (SessionManager.getUser(request).isPresent()) {
            response.sendRedirect("/calendar");
            return null;
        }

        model.addAttribute("error", request.getParameter("error") == null ? null : "Failed to log in. Username or password incorrect.");
        return "login";
    }

    @GetMapping("/register")
    public String register(HttpServletRequest request, Model model) {
        model.addAttribute("error", request.getParameter("error") == null ? null : "Failed to register. User with this username already exists.");
        return "register";
    }

    /*
     * Returns the percentage of days between a start of date and a given datetime
     *
     * Result can be negative
     */
    private double dayPercentageBetween(LocalDate date, LocalDateTime dateTime) {
        return Duration.between(date.atStartOfDay(), dateTime).toMinutes() / 24.0 / 60.0 * 100.0;
    }

    @GetMapping("/calendar")
    public String calendar(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        if (SessionManager.getUser(request).isEmpty()) {
            response.sendRedirect("/login");
            return null;
        }

        User user = SessionManager.getUser(request).get();

        int daysToShow = DAYS_TO_SHOW;
        try {
            daysToShow = Integer.parseInt(request.getParameter("n"));
        } catch (Exception ignored) {}

        List<Map<String, Object>> days = new ArrayList<>(daysToShow);
        for (int i = 0; i < daysToShow; i++) {
            LocalDate date = LocalDate.now().plusDays(i);

            Map<String, Object> day = new HashMap<>();
            List<Map<String, Object>> eventObjects = new ArrayList<>();
            {
                List<Event> events = eventDataAccessService.getAllOfUserOnDay(user, date);
                for (Event event : events) {
                    Map<String, Object> eventObject = new LinkedHashMap<>();

                    double start = dayPercentageBetween(date, event.start_time().toLocalDateTime());
                    double end = dayPercentageBetween(date, event.end_time().toLocalDateTime());

                    if (start < 0.0) {
                        start = 0.0;
                    } else {
                        eventObject.put("startedToday", true);
                    }

                    if (end > 100.0) {
                        end = 100.0;
                    } else {
                        eventObject.put("endedToday", true);
                    }

                    double top = start;
                    double height = end - start;

                    eventObject.put("id", event.id());
                    eventObject.put("title", event.title());
                    eventObject.put("description", event.description());
                    eventObject.put("top", top);
                    eventObject.put("height", height);

                    eventObjects.add(eventObject);
                }
            }

            day.put("dayOfWeek", date.getDayOfWeek().toString());
            day.put("dayDate", date.toString());
            day.put("events", eventObjects);

            days.add(day);
        }

        model.addAttribute("days", days);
        return "calendar";
    }

}
