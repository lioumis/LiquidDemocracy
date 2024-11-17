package gr.upatras.ceid.ld.validator;

import gr.upatras.ceid.ld.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    public void validateUsername(String username) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("The username is empty");
        }
    }

    public void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("The email is empty");
        }

        if (!email.contains("@")) { //TODO: Use an email validation regex
            throw new ValidationException("The email contains invalid characters");
        }
    }

    public void validatePassword(String password) throws ValidationException {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("The password is empty");
        }
    }
}
