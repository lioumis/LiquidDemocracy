package gr.upatras.ceid.ld.dto;

public record VotingResultDto(
        VotingOptionDto option,
        Integer count) {
}
