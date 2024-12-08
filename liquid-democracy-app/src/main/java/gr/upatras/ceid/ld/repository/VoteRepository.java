package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.entity.VoteEntity;
import gr.upatras.ceid.ld.entity.VotingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<VoteEntity, Long> {
    boolean existsByVoterAndVoting(UserEntity voter, VotingEntity topic);

    Optional<VoteEntity> findByVoterAndVoting(UserEntity voter, VotingEntity topic);
}