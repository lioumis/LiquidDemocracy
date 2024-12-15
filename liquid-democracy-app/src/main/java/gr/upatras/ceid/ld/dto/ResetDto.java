package gr.upatras.ceid.ld.dto;

public record ResetDto(
        String username,
        String email,
        String newPassword,
        String securityAnswer) {
}
