package gr.upatras.ceid.ld.voting.dto;

public record DiscussionDto(
        int id,
        String name,
        String surname,
        String message,
        int likes,
        int dislikes,
        Boolean userAction) {
}
