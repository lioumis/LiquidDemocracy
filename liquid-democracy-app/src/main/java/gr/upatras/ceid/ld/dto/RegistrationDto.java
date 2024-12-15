package gr.upatras.ceid.ld.dto;

public record RegistrationDto(
        String username,
        String email,
        String name,
        String surname,
        String password,
        String securityQuestion,
        String securityAnswer) {
}
