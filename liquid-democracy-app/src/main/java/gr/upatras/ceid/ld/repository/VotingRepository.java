package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.VotingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingRepository extends JpaRepository<VotingEntity, Long> {
}