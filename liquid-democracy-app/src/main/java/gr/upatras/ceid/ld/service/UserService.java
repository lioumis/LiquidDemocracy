package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.entity.AuditLogEntity;
import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.enums.Action;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.repository.AuditLogRepository;
import gr.upatras.ceid.ld.repository.UserRepository;
import gr.upatras.ceid.ld.validator.UserValidator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserValidator userValidator;

    private final AuditLogRepository auditLogRepository; //TODO: Maybe create a LoggingService

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserValidator userValidator, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    @Transactional
    public void registerUser(String username, String email, String rawPassword, String securityQuestion, String securityAnswer) throws ValidationException {
        //TODO: Τι άλλα στοιχεία κρατάμε για το χρήστη;
        //TODO: Ονοματεπώνυμο;
        userValidator.validateUsername(username);
        userValidator.validateEmail(email);
        userValidator.validatePassword(username);
        userValidator.validateSecurityQuestion(securityQuestion);
        userValidator.validateSecurityAnswer(securityAnswer);

        Optional<UserEntity> byUsername = userRepository.findByUsername(username);
        if (byUsername.isPresent()) {
            throw new ValidationException("A user with this username already exists!");
        }

        Optional<UserEntity> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            throw new ValidationException("A user with this email already exists!");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        String encodedAnswer = passwordEncoder.encode(securityAnswer);
        UserEntity newUser = new UserEntity(username, email, encodedPassword, securityQuestion, encodedAnswer);
        userRepository.save(newUser);

        auditLogRepository.save(new AuditLogEntity(newUser, Action.USER_REGISTRATION,
                "Πραγματοποιήθηκε εγγραφή του χρήστη " + username + " με email " + email + "."));
    }

    public String getSecurityQuestion(String username, String email) throws ValidationException {
        userValidator.validateUsername(username);
        userValidator.validateEmail(email);

        UserEntity user = findUser(username, email);

        return user.getSecurityQuestion();
    }

    @Transactional
    public void resetPassword(String username, String email, String securityAnswer, String newRawPassword) throws ValidationException {
        userValidator.validateUsername(username);
        userValidator.validateEmail(email);
        userValidator.validatePassword(newRawPassword);
        userValidator.validateSecurityAnswer(securityAnswer);

        UserEntity user = findUser(username, email);

        if (!passwordEncoder.matches(securityAnswer, user.getSecurityAnswerHash())) {
            throw new ValidationException("Incorrect answer");
        }

        String encodedPassword = passwordEncoder.encode(newRawPassword);
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);
        auditLogRepository.save(new AuditLogEntity(user, Action.PASSWORD_RESET,
                "Πραγματοποιήθηκε επαναφορά κωδικού πρόσβασης από το χρήστη " + username + "."));
    }

    private UserEntity findUser(String username, String email) throws ValidationException {
        Optional<UserEntity> byUsername = userRepository.findByUsername(username);
        if (byUsername.isEmpty()) {
            throw new ValidationException(USER_NOT_FOUND_MESSAGE);
        }

        UserEntity user = byUsername.get();
        if (!email.equals(user.getEmail())) {
            throw new ValidationException(USER_NOT_FOUND_MESSAGE);
        }

        return user;
    }
}