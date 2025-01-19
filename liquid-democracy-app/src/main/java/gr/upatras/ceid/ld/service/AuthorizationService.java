package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.enums.Role;
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

    public String getAuthorizedUser(String username, Set<Role> allowedRoles) throws AuthorizationException {
        Optional<UserEntity> userEntity = userRepository.findByUsername(username);

        if (userEntity.isEmpty()) {
            throw new AuthorizationException("Άγνωστος χρήστης");
        }

        UserEntity user = userEntity.get();
        if (user.getRoles().stream().noneMatch(allowedRoles::contains)) {
            throw new AuthorizationException("Δεν έχετε τον απαραίτητο ρόλο για αυτή τη λειτουργία");
        }

        return user.getUsername();
    }

}
