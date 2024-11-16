package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.exception.AuthorizationException;
import gr.upatras.ceid.ld.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class AuthorizationService {
    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long isUserAuthorized(String username, Set<String> allowedRoles) throws AuthorizationException {
        Optional<UserEntity> userEntity = userRepository.findByUsername(username);

        if (userEntity.isEmpty()) {
            throw new AuthorizationException("Unknown user");
        }

        UserEntity user = userEntity.get();
        if (!allowedRoles.contains(user.getRole())) {
            throw new AuthorizationException("The user does not have the correct role for this operation");
        }

        return user.getId();
    }

}
