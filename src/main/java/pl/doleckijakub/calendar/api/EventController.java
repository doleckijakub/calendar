package pl.doleckijakub.calendar.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.doleckijakub.calendar.dataaccess.EventDataAccessService;
import pl.doleckijakub.calendar.model.Event;

import java.util.List;

@RestController
@RequestMapping("api/v1/event")
public class EventController {

    private final EventDataAccessService eventDataAccessService;

    @Autowired
    public EventController(@Qualifier("postgresEventAS") EventDataAccessService eventDataAccessService) {
        this.eventDataAccessService = eventDataAccessService;
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventDataAccessService.getAll();
    }

}
