package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.DelegationEntity;
import gr.upatras.ceid.ld.entity.TopicEntity;
import gr.upatras.ceid.ld.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DelegationRepository extends JpaRepository<DelegationEntity, Long> {
    boolean existsByDelegatorAndTopic(UserEntity delegator, TopicEntity topic);

    Optional<DelegationEntity> findByDelegatorAndTopic(UserEntity delegator, TopicEntity topic);

    List<DelegationEntity> findByDelegateAndTopic(UserEntity delegate, TopicEntity topic);

}