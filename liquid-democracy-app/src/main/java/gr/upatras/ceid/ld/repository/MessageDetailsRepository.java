package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.MessageDetailsEntity;
import gr.upatras.ceid.ld.entity.MessageEntity;
import gr.upatras.ceid.ld.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageDetailsRepository extends JpaRepository<MessageDetailsEntity, Long> {
    Optional<MessageDetailsEntity> findByMessageAndUser(MessageEntity message, UserEntity user);
}
