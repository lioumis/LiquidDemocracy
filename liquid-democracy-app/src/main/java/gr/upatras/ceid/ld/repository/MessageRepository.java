package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.MessageEntity;
import gr.upatras.ceid.ld.entity.VotingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findByVoting(VotingEntity voting);
}