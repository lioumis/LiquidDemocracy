package gr.upatras.ceid.ld.dto;

import java.util.List;

public record VoteDto(
        Integer votingId,
        List<String> votes
) {
}
