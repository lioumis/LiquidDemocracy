package gr.upatras.ceid.ld.voting.dto;

public record RequestProcessDto(
        Long requestId,
        boolean approve
) {
}
