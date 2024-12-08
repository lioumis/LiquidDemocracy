package gr.upatras.ceid.ld.dto;

import java.util.List;

public record VotingDetailsDto(
        String name,
        String topic,
        String startDate,
        String endDate,
        String information,
        Boolean delegated,
        List<VotingResultDto> results,
        VotingOptionDto userVote,
        Integer directVotes,
        Integer delegatedVotes) {
}
