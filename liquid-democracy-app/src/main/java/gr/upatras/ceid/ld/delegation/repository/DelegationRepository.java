package gr.upatras.ceid.ld.delegation.repository;

import gr.upatras.ceid.ld.delegation.entity.DelegationEntity;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import gr.upatras.ceid.ld.voting.entity.VotingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DelegationRepository extends JpaRepository<DelegationEntity, Long> {
    boolean existsByDelegatorAndVoting(UserEntity delegator, VotingEntity voting);

    Optional<DelegationEntity> findByDelegatorAndVoting(UserEntity delegator, VotingEntity voting);

    List<DelegationEntity> findByDelegateAndVoting(UserEntity delegate, VotingEntity voting);

    List<DelegationEntity> findByDelegator(UserEntity delegator);

    List<DelegationEntity> findByDelegate(UserEntity delegate);
}