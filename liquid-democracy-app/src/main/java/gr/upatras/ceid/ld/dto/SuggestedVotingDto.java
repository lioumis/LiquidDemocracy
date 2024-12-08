package gr.upatras.ceid.ld.dto;

public record SuggestedVotingDto(
        String name,
        String topic,
        int votes,
        int comments,
        int id) {
}
