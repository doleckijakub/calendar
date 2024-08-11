package pl.doleckijakub.calendar.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pl.doleckijakub.calendar.dataaccess.EventDataAccessService;
import pl.doleckijakub.calendar.manager.SessionManager;
import pl.doleckijakub.calendar.model.Event;
import pl.doleckijakub.calendar.model.User;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("api/v1/event")
public class EventController {

    private final EventDataAccessService eventDataAccessService;
    private final Logger logger;

    @Autowired
    public EventController(@Qualifier("postgresEventAS") EventDataAccessService eventDataAccessService) {
        this.eventDataAccessService = eventDataAccessService;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @GetMapping("/all") // debug only
    public List<Event> getAllEvents() {
        return eventDataAccessService.getAll();
    }

    @PostMapping("/new")
    public void newEvent(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("start_time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start_time_ldt,
            @RequestParam("end_time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end_time_ldt,
            @RequestParam("color") String color
    ) throws IOException {
        if (SessionManager.getUser(request).isEmpty()) {
            response.sendRedirect("/login");
            return;
        }

        User user = SessionManager.getUser(request).get();

        Timestamp start_time = Timestamp.valueOf(start_time_ldt);
        Timestamp end_time = Timestamp.valueOf(end_time_ldt);

        Event event = eventDataAccessService.create(title, description, start_time, end_time, user.id(), color);

        logger.info("{} created event {}", user.id(), event.id());

        response.sendRedirect("/calendar");
    }

}
