package pl.doleckijakub.calendar.dataaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.doleckijakub.calendar.model.Event;

import java.util.List;
import java.util.UUID;

@Repository("postgresEventAS")
public class PostgresEventDataAccessService implements EventDataAccessService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostgresEventDataAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Event> getAll() {
        final String sql = "SELECT id, title, description, start_time, end_time FROM event";
        return jdbcTemplate.query(sql, (resultSet, i) -> new Event(
                UUID.fromString(resultSet.getString("id")),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getTimestamp("start_time"),
                resultSet.getTimestamp("end_time"),
                UUID.fromString(resultSet.getString("author_id"))
        ));
    }

    @Override
    public List<Event> getAllOfUserById(UUID userId) {
        return List.of();
    }

    @Override
    public Event getById(UUID eventId) {
        return null;
    }

}
