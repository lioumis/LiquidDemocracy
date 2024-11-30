package gr.upatras.ceid.ld.validator;

import gr.upatras.ceid.ld.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserValidator { //TODO: Set & validate length of Strings

    private static final String EMAIL_PATTERN = "(?i)(?>[-a-z0-9+_][-a-z0-9+_.]*[-a-z0-9+_]+@)[-a-z0-9][-a-z0-9.]*\\.[a-z]+"; //TODO: Maybe use a less restrictive one
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

    public void validateUsername(String username) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("The username is empty");
        }
    }

    public void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("The email is empty");
        }

        if (!emailPattern.matcher(email).matches()) {
            throw new ValidationException("The email is not valid");
        }
    }

    public void validateName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("The name is empty");
        }
    }

    public void validateSurname(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("The surname is empty");
        }
    }

    public void validatePassword(String password) throws ValidationException {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("The password is empty");
        }
    }

    public void validateSecurityQuestion(String question) throws ValidationException {
        if (question == null || question.trim().isEmpty()) {
            throw new ValidationException("The security question is empty");
        }
    }

    public void validateSecurityAnswer(String answer) throws ValidationException {
        if (answer == null || answer.trim().isEmpty()) {
            throw new ValidationException("The answer of the security question is empty");
        }
    }

    public void validateString(String string, String errorMessage) throws ValidationException {
        if (string == null || string.trim().isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }
}
