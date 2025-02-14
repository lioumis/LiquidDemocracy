package gr.upatras.ceid.ld.voting.dto;

import java.util.List;

public record VotingInitializationDto(
        String name,
        String topic,
        List<String> committee
) {
}
