package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.exception.ValidationException;
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

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserValidator userValidator;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public void registerUser(String username, String email, String rawPassword) throws ValidationException {
        userValidator.validateUsername(username);
        userValidator.validateEmail(email);
        userValidator.validatePassword(username);

        Optional<UserEntity> byUsername = userRepository.findByUsername(username);
        if (byUsername.isPresent()) {
            throw new ValidationException("A user with this username already exists!");
        }

        Optional<UserEntity> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            throw new ValidationException("A user with this email already exists!");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        UserEntity newUser = new UserEntity(username, email, encodedPassword);
        userRepository.save(newUser);
//        //TODO: Audit log
    }
}