package gr.upatras.ceid.ld.dto;

public record DelegationRequestDto(
        String delegator,
        String delegateName,
        String delegateSurname,
        Long topicId
) {
}
