package gr.upatras.ceid.ld.dto;

import java.util.List;

public record VotingInitializationDto(
        String name,
        String topic,
        List<String> committee
) {
}
