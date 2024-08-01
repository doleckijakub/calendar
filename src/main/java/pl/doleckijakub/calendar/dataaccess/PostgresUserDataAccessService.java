package pl.doleckijakub.calendar.dataaccess;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.doleckijakub.calendar.model.User;

import javax.naming.AuthenticationException;
import javax.persistence.EntityExistsException;
import java.util.UUID;

@Repository("postgresUserAS")
public class PostgresUserDataAccessService implements UserDataAccessService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostgresUserDataAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User register(String username, String password) throws EntityExistsException {
        final String sql = "INSERT INTO \"user\" (id, username, password) VALUES (uuid_generate_v4(), ?, ?)";
        int result = jdbcTemplate.update(
                sql,
                username,
                BCrypt.hashpw(password, BCrypt.gensalt())
        );

        if (result != 1) throw new EntityExistsException();

        try {
            return login(username, password);
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User login(String username, String password) throws AuthenticationException {
        final String sql = "SELECT id, password FROM \"user\" WHERE username = ?";
        User result = jdbcTemplate.queryForObject(sql, (resultSet, i) -> {
            if (BCrypt.checkpw(password, resultSet.getString("password"))) return new User(
                    UUID.fromString(resultSet.getString("id")),
                    username
            );

            return null;
        }, username);

        if (result == null) throw new AuthenticationException();

        return result;
    }

}
