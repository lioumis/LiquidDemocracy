package gr.upatras.ceid.ld.voting.repository;

import gr.upatras.ceid.ld.user.entity.UserEntity;
import gr.upatras.ceid.ld.voting.entity.VoteEntity;
import gr.upatras.ceid.ld.voting.entity.VotingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<VoteEntity, Long> {
    boolean existsByOriginalVoterAndVoting(UserEntity voter, VotingEntity voting);

    Optional<VoteEntity> findByOriginalVoterAndVoting(UserEntity voter, VotingEntity voting);
}