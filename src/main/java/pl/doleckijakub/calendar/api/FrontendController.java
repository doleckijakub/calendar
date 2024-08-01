package pl.doleckijakub.calendar.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
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

    @GetMapping("/calendar")
    public String calendar(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        if (SessionManager.getUser(request).isEmpty()) {
            response.sendRedirect("/login");
            return null;
        }

        User user = SessionManager.getUser(request).get();

        List<Map<String, Object>> days = new ArrayList<>(DAYS_TO_SHOW);
        for (int i = 0; i < DAYS_TO_SHOW; i++) {
            LocalDate date = LocalDate.now().plusDays(i);

            Map<String, Object> day = new HashMap<>();
            List<Map<String, Object>> eventObjects = new ArrayList<>();
            {
                List<Event> events = eventDataAccessService.getAllOfUserOnDay(user, date);
                for (Event event : events) {
                    LoggerFactory.getLogger(getClass()).info("on {} {}", date, event);

                    Map<String, Object> eventObject = new LinkedHashMap<>();

                    LocalDateTime start = event.start_time().toLocalDateTime();
                    long startOfDayMinutes = start.getHour() * 60 + start.getMinute();
                    double top = (startOfDayMinutes / 1440.0) * 100 /* % */;

                    LocalDateTime end = event.end_time().toLocalDateTime();
                    Duration duration = Duration.between(start, end);
                    long durationMinutes = duration.toMinutes();
                    double height = (durationMinutes / 1440.0) * 100 /* % */;

                    eventObject.put("id", event.id());
                    eventObject.put("title", event.title());
                    eventObject.put("description", event.description());
                    eventObject.put("top", top);
                    eventObject.put("height", height);

                    eventObjects.add(eventObject);
                }
            }
            day.put("i", i);
            day.put("dayOfWeek", date.getDayOfWeek().toString());
            day.put("dayDate", date.toString());
            day.put("events", eventObjects);

            days.add(day);
        }

        model.addAttribute("days", days);
        return "calendar";
    }

}
