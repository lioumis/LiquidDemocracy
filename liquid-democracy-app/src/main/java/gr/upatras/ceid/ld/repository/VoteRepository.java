package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.TopicEntity;
import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.entity.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<VoteEntity, Long> {
    boolean existsByVoterAndTopic(UserEntity voter, TopicEntity topic);
}