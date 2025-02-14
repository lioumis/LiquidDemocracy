package gr.upatras.ceid.ld.delegation.dto;

public record DelegationRequestDto(
        String delegator,
        String delegateName,
        String delegateSurname,
        Long votingId
) {
}
