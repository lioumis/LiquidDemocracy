package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.DiscussionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionRepository extends JpaRepository<DiscussionEntity, Long> {
}