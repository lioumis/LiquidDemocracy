package gr.upatras.ceid.ld.common.authorization;

import gr.upatras.ceid.ld.common.enums.Role;
import gr.upatras.ceid.ld.common.exception.AuthorizationException;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import gr.upatras.ceid.ld.user.repository.UserRepository;
import gr.upatras.ceid.ld.voting.entity.VotingEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public UserEntity getAuthorizedUser(String username) throws AuthorizationException {
        Optional<UserEntity> userEntity = userRepository.findByUsername(username);

        if (userEntity.isEmpty()) {
            throw new AuthorizationException("Άγνωστος χρήστης");
        }

        return userEntity.get();
    }

    public void checkRoles(UserEntity user, Set<Role> allowedRoles) throws AuthorizationException {
        Set<Role> userRoles = user.getRoles();

        if (!userRoles.contains(Role.REPRESENTATIVE)) {
            Set<VotingEntity> byDelegate = user.getDelegatableVotings();
            for (VotingEntity votingEntity : byDelegate) {
                LocalDateTime endDate = votingEntity.getEndDate();

                if (votingEntity.isValid() && endDate != null && endDate.isAfter(LocalDateTime.now())) {
                    userRoles.add(Role.REPRESENTATIVE);
                    break;
                }
            }
        }

        if (userRoles.stream().noneMatch(allowedRoles::contains)) {
            throw new AuthorizationException("Δεν έχετε τον απαραίτητο ρόλο για αυτή τη λειτουργία");
        }
    }

}
