package gr.upatras.ceid.ld.user.repository;

import gr.upatras.ceid.ld.common.enums.Role;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByNameAndSurnameIgnoreCase(String name, String surname);

    List<UserEntity> findByRolesContains(Set<Role> roles);
}