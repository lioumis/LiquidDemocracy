package gr.upatras.ceid.ld.user.dto;

public record ResetDto(
        String username,
        String email,
        String newPassword,
        String securityAnswer) {
}
