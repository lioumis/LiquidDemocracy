package gr.upatras.ceid.ld.validator;

import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.enums.Role;
import gr.upatras.ceid.ld.exception.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserValidator {
    private final PasswordEncoder passwordEncoder;

    private static final String EMAIL_PATTERN = "(?i)(?>[-a-z0-9+_][-a-z0-9+_.]*[-a-z0-9+_]+@)[-a-z0-9][-a-z0-9.]*\\.[a-z]+";
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

    public UserValidator(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void validateUsername(String username) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Το όνομα χρήστη είναι κενό");
        }
        if (username.length() > 255) {
            throw new ValidationException("Το όνομα χρήστη είναι υπερβολικά μεγάλο");
        }
    }

    public void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Η διεύθυνση Email είναι κενή");
        }

        if (email.length() > 255) {
            throw new ValidationException("Η διεύθυνση Email είναι υπερβολικά μεγάλη");
        }

        if (!emailPattern.matcher(email).matches()) {
            throw new ValidationException("Η διεύθυνση Email δεν είναι έγκυρη");
        }
    }

    public void validateName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Το όνομα είναι κενό");
        }

        if (name.length() > 255) {
            throw new ValidationException("Το όνομα είναι υπερβολικά μεγάλο");
        }
    }

    public void validateSurname(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Το επώνυμο είναι κενό");
        }

        if (name.length() > 255) {
            throw new ValidationException("Το επώνυμο είναι υπερβολικά μεγάλο");
        }
    }

    public void validatePassword(String password) throws ValidationException {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Ο κωδικός πρόσβασης είναι κενός");
        }
    }

    public void validateSecurityQuestion(String question) throws ValidationException {
        if (question == null || question.trim().isEmpty()) {
            throw new ValidationException("Η ερώτηση ασφαλείας είναι κενή");
        }
        if (question.length() > 255) {
            throw new ValidationException("Η ερώτηση ασφαλείας είναι υπερβολικά μεγάλη");
        }
    }

    public void validateSecurityAnswer(String answer) throws ValidationException {
        if (answer == null || answer.trim().isEmpty()) {
            throw new ValidationException("Η απάντηση ασφαλείας είναι κενή");
        }
    }

    public void validateString(String string, String errorMessage) throws ValidationException {
        if (string == null || string.trim().isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }

    public Role validateRole(UserEntity userEntity, String roleString) throws ValidationException {
        Role role;
        try {
            role = Role.fromName(roleString);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Ο ρόλος δεν βρέθηκε");
        }

        if (userEntity.getRoles().contains(role)) {
            throw new ValidationException("Ο χρήστης έχει ήδη το συγκεκριμένο ρόλο");
        }

        return role;
    }

    public void checkSecurityAnswer(String answer, String existingHash) throws ValidationException {
        if (!passwordEncoder.matches(answer, existingHash)) {
            throw new ValidationException("Λανθασμένη απάντηση");
        }
    }

    public void checkOldPassword(String oldPassword, String existingHash) throws ValidationException {
        if (!passwordEncoder.matches(oldPassword, existingHash)) {
            throw new ValidationException("Ο παλιός κωδικός πρόσβασης είναι εσφαλμένος");
        }
    }
}
