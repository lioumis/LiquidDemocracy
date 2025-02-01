package gr.upatras.ceid.ld.dto;

import java.util.List;

public record VotingInitializationDto(
        String name,
        Long topic,
        List<String> committee
) {
}
