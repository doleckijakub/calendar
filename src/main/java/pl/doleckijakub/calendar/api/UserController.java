package pl.doleckijakub.calendar.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import pl.doleckijakub.calendar.dataaccess.UserDataAccessService;
import pl.doleckijakub.calendar.model.User;

import javax.naming.AuthenticationException;
import javax.persistence.EntityExistsException;

@RestController
@RequestMapping("api/v1/user")
public class UserController {

    private final UserDataAccessService userDataAccessService;

    @Autowired
    public UserController(@Qualifier("postgresUserAS") UserDataAccessService userDataAccessService) {
        this.userDataAccessService = userDataAccessService;
    }

    @PostMapping("/register")
    public User register(@RequestParam("username") String username, @RequestParam("password") String password) throws EntityExistsException {
        return userDataAccessService.register(username, password);
    }

    @PostMapping("/login")
    public User login(@RequestParam("username") String username, @RequestParam("password") String password) throws AuthenticationException {
        return userDataAccessService.login(username, password);
    }

}
