package gr.upatras.ceid.ld.voting.repository;

import gr.upatras.ceid.ld.user.entity.UserEntity;
import gr.upatras.ceid.ld.voting.entity.ParticipantEntity;
import gr.upatras.ceid.ld.voting.entity.VotingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<ParticipantEntity, Long> {
    boolean existsByUserAndVoting(UserEntity user, VotingEntity voting);

    Optional<ParticipantEntity> findByUserAndVoting(UserEntity user, VotingEntity voting);

    List<ParticipantEntity> findByVotingAndStatusIs(VotingEntity voting, Boolean status);

    void deleteAllByUser(UserEntity user);
}
