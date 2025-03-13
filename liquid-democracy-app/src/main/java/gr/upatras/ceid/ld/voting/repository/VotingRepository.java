package gr.upatras.ceid.ld.voting.repository;

import gr.upatras.ceid.ld.voting.entity.VotingEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VotingRepository extends JpaRepository<VotingEntity, Long> {

    @Query("""
                SELECT v
                FROM voting v
                LEFT JOIN v.votes votes
                LEFT JOIN v.messages messages
                WHERE v.startDate IS NOT NULL
                AND v.endDate IS NOT NULL
                AND v.endDate > CURRENT_TIMESTAMP
                GROUP BY v.id
                ORDER BY COUNT(votes) + COUNT(messages) DESC
            """)
    List<VotingEntity> findTopVotingsWithMostVotesAndComments(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
