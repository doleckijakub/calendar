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

import java.awt.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

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

    private int numEventsAtTime(List<Event> events, Timestamp timestamp) {
        int result = 0;

        for (Event event : events) {
            if (event.start_time().before(timestamp) && (event.end_time().equals(timestamp) || event.end_time().after(timestamp))) {
                result += 1;
            }
        }

        return result;
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

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        List<Map<String, Object>> days = new ArrayList<>(daysToShow);
        for (int i = 0; i < daysToShow; i++) {
            LocalDate date = LocalDate.now().plusDays(i);

            Map<String, Object> day = new HashMap<>();
            List<Map<String, Object>> eventObjects = new ArrayList<>();
            {
                List<Event> events = eventDataAccessService.getAllOfUserOnDay(user, date);

                Set<Timestamp> uniqueTimestamps = new HashSet<>();
                for (Event event : events) {
                    uniqueTimestamps.add(event.start_time());
                    uniqueTimestamps.add(event.end_time());
                }

                int maxEventsAtATime = 0;
                Map<Timestamp, Integer> numEventsAtATime = new HashMap<>();
                for (Timestamp timestamp : uniqueTimestamps) {
                    int c = 0;
                    
                    for (Event event : events) {
                        if (event.start_time().before(timestamp) && (event.end_time().after(timestamp) || event.end_time().equals(timestamp))) { // start_time < timestamp <= end_time
                            c++;
                        }
                    }

                    if (c > maxEventsAtATime) maxEventsAtATime = c;

                    numEventsAtATime.put(timestamp, c);
                }

                for (Event event : events) {
                    Map<String, Object> eventObject = new LinkedHashMap<>();

                    double start = dayPercentageBetween(date, event.start_time().toLocalDateTime());
                    double end = event.end_time() == null ? start + 100.0 / 24.0 : dayPercentageBetween(date, event.end_time().toLocalDateTime());

                    if (start < 0.0) {
                        start = 0.0;
                    } else {
                        eventObject.put("startedToday", true);
                        eventObject.put("startTime", timeFormat.format(event.start_time()));
                    }

                    if (end > 100.0) {
                        end = 100.0;
                    } else {
                        eventObject.put("endedToday", true);
                        eventObject.put("endTime", timeFormat.format(event.end_time()));
                    }

                    int index = Math.min(numEventsAtATime.get(event.start_time()), numEventsAtATime.get(event.end_time()));

                    double left = 100.0 * index / maxEventsAtATime;
                    double top = start;
                    double width = 100.0 / maxEventsAtATime;
                    double height = end - start;

                    eventObject.put("id", event.id());
                    eventObject.put("title", event.title());
                    eventObject.put("description", event.description());
                    eventObject.put("color", event.color());
                    eventObject.put("borderColor", hexColorMultiply(event.color(), 0.5f));

                    eventObject.put("left", left);
                    eventObject.put("top", top);
                    eventObject.put("width", width);
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

    private String hexColorMultiply(String hexColor, float v) {
        Color color = Color.decode(hexColor);

        int r = (int)(color.getRed() * v);
        int g = (int)(color.getGreen() * v);
        int b = (int)(color.getBlue() * v);

        return String.format("#%02x%02x%02x", r, g, b);
    }

}
