package pl.doleckijakub.calendar.dataaccess;

import pl.doleckijakub.calendar.model.Event;
import pl.doleckijakub.calendar.model.User;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventDataAccessService {

    List<Event> getAll();
    List<Event> getAllOfUserOnDay(User user, LocalDate date);
    Event getById(UUID eventId);

    Event create(String title, String description, Timestamp startTime, Timestamp endTime, UUID authorId);
}
