package gr.upatras.ceid.ld.dto;

public record ChangePasswordDto(
        String oldPassword,
        String newPassword
) {
}
