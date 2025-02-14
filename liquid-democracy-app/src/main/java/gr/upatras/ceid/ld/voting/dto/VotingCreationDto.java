package gr.upatras.ceid.ld.voting.dto;

import java.util.List;

public record VotingCreationDto(
        Long id,
        String startDate,
        String endDate,
        String description,
        String mechanism,
        List<VotingOptionDto> options,
        Long voteLimit
) {
}
