package gr.upatras.ceid.ld.dto;

import java.util.Set;

public record UserInformationDto(
        String username,
        String name,
        String surname,
        String email,
        Set<String> roles) {
}
