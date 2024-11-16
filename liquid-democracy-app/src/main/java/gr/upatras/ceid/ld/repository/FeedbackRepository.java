package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
}