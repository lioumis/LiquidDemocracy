package gr.upatras.ceid.ld.dto;

import java.util.List;

public record VotingCreationDto(
        String name,
        String topic,
        String startDate,
        String endDate,
        String description,
        String mechanism,
        List<VotingOptionDto> options,
        String comment
) {
}
