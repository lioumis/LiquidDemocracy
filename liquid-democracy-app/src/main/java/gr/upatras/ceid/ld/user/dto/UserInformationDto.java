package gr.upatras.ceid.ld.user.dto;

import java.util.Set;

public record UserInformationDto(
        Long id,
        String username,
        String name,
        String surname,
        String email,
        Set<String> roles) {
}
