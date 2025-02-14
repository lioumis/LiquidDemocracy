package gr.upatras.ceid.ld.voting.dto;

public record SuggestedVotingDto(
        String name,
        String topic,
        int votes,
        int comments,
        int id) {
}
