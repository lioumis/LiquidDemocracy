package gr.upatras.ceid.ld.voting.dto;

import java.util.List;

public record VotingDetailsDto(
        String name,
        String topic,
        String startDate,
        String endDate,
        String information,
        Boolean delegated,
        Integer votingType,
        Integer voteLimit,
        List<VotingResultDto> results,
        List<VotingOptionDto> userVote,
        Integer directVotes,
        Integer delegatedVotes,
        String feedback) {
}
