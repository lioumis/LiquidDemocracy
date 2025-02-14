package gr.upatras.ceid.ld.user.dto;

public record ChangePasswordDto(
        String oldPassword,
        String newPassword
) {
}
