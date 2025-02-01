package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.FeedbackEntity;
import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.entity.VotingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    boolean existsByVotingAndUser(VotingEntity voting, UserEntity user);

    Optional<FeedbackEntity> findByVotingAndUser(VotingEntity voting, UserEntity user);

    List<FeedbackEntity> findByVoting(VotingEntity voting);
}
