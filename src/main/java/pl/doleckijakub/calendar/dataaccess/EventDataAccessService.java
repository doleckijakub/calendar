package pl.doleckijakub.calendar.dataaccess;

import org.springframework.stereotype.Repository;
import pl.doleckijakub.calendar.model.Event;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventDataAccessService {

    List<Event> getAll();
    List<Event> getAllOfUserById(UUID userId);
    Event getById(UUID eventId);

}
