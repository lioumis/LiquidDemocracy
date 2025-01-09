package gr.upatras.ceid.ld.dto;

import java.util.List;

public record VotingDetailsDto(
        String name,
        String topic,
        String startDate,
        String endDate,
        String information,
        Boolean delegated,
        Integer votingType,
        List<VotingResultDto> results,
        VotingOptionDto userVote,
        Integer directVotes,
        Integer delegatedVotes,
        String feedback) {
}
