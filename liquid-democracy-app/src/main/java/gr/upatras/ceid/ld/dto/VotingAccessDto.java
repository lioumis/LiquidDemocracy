package gr.upatras.ceid.ld.dto;

public record VotingAccessDto(
        boolean isPresent,
        Boolean hasAccess //TODO: Handle correctly on UI
) {
}
