package pl.doleckijakub.calendar.dataaccess;

import org.springframework.stereotype.Repository;
import pl.doleckijakub.calendar.model.User;

import javax.naming.AuthenticationException;
import javax.persistence.EntityExistsException;

@Repository
public interface UserDataAccessService {

    User register(String username, String password) throws EntityExistsException;
    User login(String username, String password) throws AuthenticationException;

}
