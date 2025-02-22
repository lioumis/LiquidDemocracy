package gr.upatras.ceid.ld.user.service;

import gr.upatras.ceid.ld.common.auditlog.service.LoggingService;
import gr.upatras.ceid.ld.common.enums.Action;
import gr.upatras.ceid.ld.common.enums.Role;
import gr.upatras.ceid.ld.common.exception.ValidationException;
import gr.upatras.ceid.ld.user.dto.UserInformationDto;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import gr.upatras.ceid.ld.user.repository.UserRepository;
import gr.upatras.ceid.ld.user.validator.UserValidator;
import gr.upatras.ceid.ld.voting.entity.ParticipantEntity;
import gr.upatras.ceid.ld.voting.entity.VotingEntity;
import gr.upatras.ceid.ld.voting.repository.ParticipantRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private static final String USER_NOT_FOUND_MESSAGE = "Ο χρήστης δεν βρέθηκε";

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserValidator userValidator;

    private final LoggingService loggingService;

    private final ParticipantRepository participantRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserValidator userValidator,
                       LoggingService loggingService, ParticipantRepository participantRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
        this.loggingService = loggingService;
        this.participantRepository = participantRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    @Transactional
    public void registerUser(String username, String email, String name, String surname, String rawPassword,
                             String securityQuestion, String securityAnswer) throws ValidationException {
        userValidator.validateUsername(username);
        userValidator.validateEmail(email);
        userValidator.validateName(name);
        userValidator.validateSurname(surname);
        userValidator.validatePassword(username);
        userValidator.validateSecurityQuestion(securityQuestion);
        userValidator.validateSecurityAnswer(securityAnswer);

        Optional<UserEntity> byUsername = userRepository.findByUsername(username);
        if (byUsername.isPresent()) {
            throw new ValidationException("Υπάρχει ήδη χρήστης με αυτό το όνομα χρήστη");
        }

        Optional<UserEntity> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            throw new ValidationException("Υπάρχει ήδη χρήστης με αυτή τη διεύθυνση Email");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        String encodedAnswer = passwordEncoder.encode(securityAnswer);
        UserEntity newUser = new UserEntity(username, email, name, surname, encodedPassword, securityQuestion, encodedAnswer);
        userRepository.save(newUser);

        loggingService.log(newUser, Action.USER_REGISTRATION, "Πραγματοποιήθηκε εγγραφή του χρήστη " + username + " με email " + email + ".");
    }

    public String getSecurityQuestion(String username, String email) throws ValidationException {
        userValidator.validateUsername(username);
        userValidator.validateEmail(email);

        UserEntity user = findUser(username, email);

        return user.getSecurityQuestion();
    }

    public UserInformationDto getUserDetails(String username) throws ValidationException {
        UserEntity user = findUser(username);
        return new UserInformationDto(user.getId(), user.getUsername(), user.getName(), user.getSurname(), user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }

    public List<UserInformationDto> getAllUserDetails() {
        return userRepository.findAll().stream().map(user ->
                new UserInformationDto(user.getId(), user.getUsername(), user.getName(), user.getSurname(), user.getEmail(),
                        user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))).toList();

    }

    @Transactional
    public void addRole(String adminUsername, Long userId, String roleString) throws ValidationException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND_MESSAGE));

        UserEntity admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND_MESSAGE));

        Role role = userValidator.validateRoleExists(user, roleString);

        user.getRoles().add(role);
        userRepository.save(user);

        loggingService.log(admin, Action.NEW_ROLE, "Ο ρόλος " + role.getName() + " δόθηκε από το χρήστη " +
                adminUsername + " στο χρήστη " + user.getUsername() + ".");
    }

    @Transactional
    public void revokeRole(String adminUsername, Long userId, String roleString) throws ValidationException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND_MESSAGE));

        UserEntity admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND_MESSAGE));

        Role role = userValidator.validateRoleExistsNot(user, roleString);

        user.getRoles().remove(role);

        if (Role.ELECTORAL_COMMITTEE.equals(role)) {
            Set<VotingEntity> overseenVotings = user.getOverseenVotings();
            overseenVotings.forEach(overseenVoting -> {
                ParticipantEntity participantEntity = new ParticipantEntity(user, overseenVoting, true);
                participantRepository.save(participantEntity);
            });
            user.removeAllVotings();
        }

        userRepository.save(user);

        loggingService.log(admin, Action.NEW_ROLE, "Ο ρόλος " + role.getName() + " του χρήστη " +
                user.getUsername() + " ανακλήθηκε από το χρήστη " + adminUsername + ".");
    }

    @Transactional
    public void resetPassword(String username, String email, String securityAnswer, String newRawPassword) throws ValidationException {
        userValidator.validateUsername(username);
        userValidator.validateEmail(email);
        userValidator.validatePassword(newRawPassword);
        userValidator.validateSecurityAnswer(securityAnswer);

        UserEntity user = findUser(username, email);

        userValidator.checkSecurityAnswer(securityAnswer, user.getSecurityAnswerHash());

        String encodedPassword = passwordEncoder.encode(newRawPassword);
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);
        loggingService.log(user, Action.PASSWORD_RESET, "Πραγματοποιήθηκε επαναφορά κωδικού πρόσβασης από το χρήστη " + username + ".");
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newRawPassword) throws ValidationException {
        userValidator.validateString(oldPassword, "Ο παλιός κωδικός πρόσβασης είναι κενός");
        userValidator.validateString(newRawPassword, "Ο νέος κωδικός πρόσβασης είναι κενός");

        UserEntity user = findUser(username);

        userValidator.checkOldPassword(oldPassword, user.getPasswordHash());

        String encodedPassword = passwordEncoder.encode(newRawPassword);
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);
        loggingService.log(user, Action.PASSWORD_CHANGE, "Πραγματοποιήθηκε αλλαγή κωδικού πρόσβασης από το χρήστη " + username + ".");
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

    private UserEntity findUser(String username) throws ValidationException {
        Optional<UserEntity> byUsername = userRepository.findByUsername(username);
        if (byUsername.isEmpty()) {
            throw new ValidationException(USER_NOT_FOUND_MESSAGE);
        }

        return byUsername.get();
    }
}