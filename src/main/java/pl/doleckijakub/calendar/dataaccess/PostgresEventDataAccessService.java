package pl.doleckijakub.calendar.dataaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.doleckijakub.calendar.model.Event;
import pl.doleckijakub.calendar.model.User;

import java.sql.Timestamp;
import java.time.LocalDate;
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
        final String sql = "SELECT id, title, description, start_time, end_time, author_id FROM event";
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
    public List<Event> getAllOfUserOnDay(User user, LocalDate date) {
        final String sql = "SELECT id, title, description, start_time, end_time, author_id FROM event WHERE author_id = ? AND DATE(?) BETWEEN DATE(start_time) AND DATE(end_time)";
        return jdbcTemplate.query(sql, (resultSet, i) -> new Event(
                UUID.fromString(resultSet.getString("id")),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getTimestamp("start_time"),
                resultSet.getTimestamp("end_time"),
                UUID.fromString(resultSet.getString("author_id"))
        ), user.id(), date);
    }

    @Override
    public Event getById(UUID eventId) {
        final String sql = "SELECT id, title, description, start_time, end_time, author_id FROM event WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (resultSet, i) -> new Event(
                UUID.fromString(resultSet.getString("id")),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getTimestamp("start_time"),
                resultSet.getTimestamp("end_time"),
                UUID.fromString(resultSet.getString("author_id"))
        ), eventId);
    }

    @Override
    public Event create(String title, String description, Timestamp startTime, Timestamp endTime, UUID authorId) {
        UUID id = UUID.randomUUID();

        final String sql = "INSERT INTO \"event\" (id, title, description, start_time, end_time, author_id) VALUES (?, ?, ?, ?, ?, ?)";
        int result = jdbcTemplate.update(
                sql,
                id,
                title,
                description,
                startTime,
                endTime,
                authorId
        );

        if (result != 1) throw new RuntimeException();

        return getById(id);
    }

}
