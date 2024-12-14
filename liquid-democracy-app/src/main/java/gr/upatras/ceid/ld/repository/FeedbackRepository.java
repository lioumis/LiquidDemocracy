package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.FeedbackEntity;
import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.entity.VotingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    boolean existsByVotingAndUser(VotingEntity voting, UserEntity user);
}