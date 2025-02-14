package gr.upatras.ceid.ld.user.repository;

import gr.upatras.ceid.ld.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByNameAndSurnameIgnoreCase(String name, String surname);
}