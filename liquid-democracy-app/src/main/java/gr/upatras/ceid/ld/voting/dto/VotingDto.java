package gr.upatras.ceid.ld.voting.dto;

public record VotingDto(
        String name,
        String topic,
        String startDate,
        String endDate,
        Boolean valid,
        boolean hasVoted,
        int votes,
        int id) {
}
