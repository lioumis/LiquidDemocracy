package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.TopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<TopicEntity, Long> {
    boolean existsByTitleIgnoreCase(String title);

    Optional<TopicEntity> findByTitle(String title);
}
