package gr.upatras.ceid.ld.voting.dto;

public record VotingResultDto(
        VotingOptionDto option,
        Integer count) {
}
