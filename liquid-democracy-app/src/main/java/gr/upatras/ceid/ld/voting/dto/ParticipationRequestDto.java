package gr.upatras.ceid.ld.voting.dto;

public record ParticipationRequestDto(
        int id,
        String name,
        String surname,
        String username
) {
}
