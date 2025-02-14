package gr.upatras.ceid.ld.voting.repository;

import gr.upatras.ceid.ld.voting.entity.MessageDetailsEntity;
import gr.upatras.ceid.ld.voting.entity.MessageEntity;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageDetailsRepository extends JpaRepository<MessageDetailsEntity, Long> {
    Optional<MessageDetailsEntity> findByMessageAndUser(MessageEntity message, UserEntity user);
}
