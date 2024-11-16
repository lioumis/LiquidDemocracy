package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public void registerUser(String username, String email, String rawPassword) throws ValidationException {
        //TODO: validation...
        if (username == null || email == null || rawPassword == null) {
            throw new ValidationException("Empty values!");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        UserEntity newUser = new UserEntity(username, email, encodedPassword);
        userRepository.save(newUser);
//        //TODO: Audit log
    }
}