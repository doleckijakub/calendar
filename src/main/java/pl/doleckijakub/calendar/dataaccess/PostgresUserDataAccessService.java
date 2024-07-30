package pl.doleckijakub.calendar.dataaccess;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.doleckijakub.calendar.model.User;

import javax.naming.AuthenticationException;
import javax.persistence.EntityExistsException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Repository("postgresUserAS")
public class PostgresUserDataAccessService implements UserDataAccessService {

    private final JdbcTemplate jdbcTemplate;
    private final Logger logger;

    @Autowired
    public PostgresUserDataAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.logger = LoggerFactory.getLogger(PostgresUserDataAccessService.class);
    }

    @Override
    public User register(String username, String password) throws EntityExistsException {
        final String sql = "INSERT INTO \"user\" (id, username, password) VALUES (uuid_generate_v4(), ?, ?)";
        int result = jdbcTemplate.update(
                sql,
                username,
                BCrypt.hashpw(password, BCrypt.gensalt())
        );

        logger.info("Created user {}: {}", username, result);

        try {
            return login(username, password);
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User login(String username, String password) throws AuthenticationException {
        AtomicReference<User> result = new AtomicReference<>();

        final String sql = "SELECT id, password FROM \"user\" WHERE username = ?";
        jdbcTemplate.query(sql, (resultSet, i) -> {
            User user = new User(
                    UUID.fromString(resultSet.getString("id")),
                    username
            );

            if (BCrypt.checkpw(password, resultSet.getString("password"))) result.set(user);

            return user;
        }, username);

        if (result.get() == null) throw new AuthenticationException();

        return result.get();
    }

}
