package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
}